package com.saulpos.server.shift;

import com.saulpos.api.shift.CashShiftOpenRequest;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import com.saulpos.server.shift.service.ShiftService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ShiftOpenConcurrencyIntegrationTest {

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private StoreLocationRepository storeLocationRepository;

    @Autowired
    private TerminalDeviceRepository terminalDeviceRepository;

    private ExecutorService executorService;

    private Long cashierUserId;
    private Long terminalDeviceId;

    @BeforeEach
    void setUp() {
        executorService = Executors.newFixedThreadPool(2);

        jdbcTemplate.execute("DELETE FROM cash_movement");
        jdbcTemplate.execute("DELETE FROM cash_shift");
        jdbcTemplate.execute("DELETE FROM store_user_assignment");
        jdbcTemplate.execute("DELETE FROM terminal_device");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");
        jdbcTemplate.execute("DELETE FROM auth_audit_event");
        jdbcTemplate.execute("DELETE FROM auth_session");
        jdbcTemplate.execute("DELETE FROM user_role");
        jdbcTemplate.execute("DELETE FROM role_permission");
        jdbcTemplate.execute("DELETE FROM app_permission");
        jdbcTemplate.execute("DELETE FROM app_role");
        jdbcTemplate.execute("DELETE FROM user_account");

        UserAccountEntity cashier = new UserAccountEntity();
        cashier.setUsername("cashier-race");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashierUserId = userAccountRepository.save(cashier).getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-RACE");
        merchant.setName("Merchant Race");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode("STORE-RACE");
        store.setName("Store Race");
        store.setActive(true);
        store = storeLocationRepository.save(store);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode("TERM-RACE");
        terminal.setName("Terminal Race");
        terminal.setActive(true);
        terminalDeviceId = terminalDeviceRepository.save(terminal).getId();
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void onlyOneOpenShiftIsCreatedWhenOpenRequestsRace() throws Exception {
        CashShiftOpenRequest request = new CashShiftOpenRequest(cashierUserId, terminalDeviceId, new BigDecimal("25.00"));

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger conflictCount = new AtomicInteger();
        AtomicInteger unexpectedFailureCount = new AtomicInteger();

        Runnable openShift = () -> {
            try {
                ready.countDown();
                start.await(5, TimeUnit.SECONDS);
                shiftService.openShift(request);
                successCount.incrementAndGet();
            } catch (BaseException exception) {
                if (exception.getErrorCode() == ErrorCode.CONFLICT) {
                    conflictCount.incrementAndGet();
                } else {
                    unexpectedFailureCount.incrementAndGet();
                }
            } catch (Exception exception) {
                unexpectedFailureCount.incrementAndGet();
            } finally {
                done.countDown();
            }
        };

        executorService.submit(openShift);
        executorService.submit(openShift);

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(10, TimeUnit.SECONDS)).isTrue();

        Integer openShiftCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM cash_shift WHERE status = 'OPEN'", Integer.class);

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(conflictCount.get()).isEqualTo(1);
        assertThat(unexpectedFailureCount.get()).isZero();
        assertThat(openShiftCount).isEqualTo(1);
    }
}
