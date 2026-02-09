package com.saulpos.server.receipt;

import com.saulpos.api.receipt.ReceiptAllocationRequest;
import com.saulpos.api.receipt.ReceiptAllocationResponse;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.receipt.service.ReceiptService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.NavigableSet;
import java.util.Queue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ReceiptAllocationConcurrencyIntegrationTest {

    private static final int CONCURRENT_REQUESTS = 8;

    @Autowired
    private ReceiptService receiptService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private StoreLocationRepository storeLocationRepository;

    @Autowired
    private TerminalDeviceRepository terminalDeviceRepository;

    private ExecutorService executorService;

    private Long terminalDeviceId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
        executorService = Executors.newFixedThreadPool(CONCURRENT_REQUESTS);

        jdbcTemplate.execute("DELETE FROM receipt_header");
        jdbcTemplate.execute("DELETE FROM receipt_sequence");
        jdbcTemplate.execute("DELETE FROM receipt_series");
        jdbcTemplate.execute("DELETE FROM rounding_policy");
        jdbcTemplate.execute("DELETE FROM store_tax_rule");
        jdbcTemplate.execute("DELETE FROM open_price_entry_audit");
        jdbcTemplate.execute("DELETE FROM store_price_override");
        jdbcTemplate.execute("DELETE FROM price_book_item");
        jdbcTemplate.execute("DELETE FROM price_book");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM inventory_movement");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM tax_group");
        jdbcTemplate.execute("DELETE FROM category");
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

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-D3-RACE");
        merchant.setName("Merchant D3 Race");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode("STORE-D3-RACE");
        store.setName("Store D3 Race");
        store.setActive(true);
        store = storeLocationRepository.save(store);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode("TERM-D3-RACE");
        terminal.setName("Terminal D3 Race");
        terminal.setActive(true);
        terminalDeviceId = terminalDeviceRepository.save(terminal).getId();

        receiptService.allocate(new ReceiptAllocationRequest(terminalDeviceId));
    }

    @AfterEach
    void tearDown() {
        executorService.shutdownNow();
    }

    @Test
    void concurrentAllocationsProduceUniqueNumbers() throws Exception {
        CountDownLatch ready = new CountDownLatch(CONCURRENT_REQUESTS);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(CONCURRENT_REQUESTS);

        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failureCount = new AtomicInteger();
        NavigableSet<Long> allocatedNumbers = new ConcurrentSkipListSet<>();
        Queue<String> failures = new ConcurrentLinkedQueue<>();

        for (int i = 0; i < CONCURRENT_REQUESTS; i++) {
            executorService.submit(() -> {
                try {
                    ready.countDown();
                    start.await(5, TimeUnit.SECONDS);
                    ReceiptAllocationResponse response = receiptService.allocate(new ReceiptAllocationRequest(terminalDeviceId));
                    allocatedNumbers.add(response.number());
                    successCount.incrementAndGet();
                } catch (Exception exception) {
                    failureCount.incrementAndGet();
                    failures.add(exception.getClass().getSimpleName() + ": " + exception.getMessage());
                } finally {
                    done.countDown();
                }
            });
        }

        assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
        start.countDown();
        assertThat(done.await(20, TimeUnit.SECONDS)).isTrue();

        Integer totalHeaders = jdbcTemplate.queryForObject("SELECT count(*) FROM receipt_header", Integer.class);
        Integer distinctNumbers = jdbcTemplate.queryForObject("SELECT count(DISTINCT number) FROM receipt_header", Integer.class);

        assertThat(failureCount.get())
                .withFailMessage("unexpected allocation failures: %s", failures)
                .isZero();
        assertThat(successCount.get()).isEqualTo(CONCURRENT_REQUESTS);
        assertThat(allocatedNumbers).hasSize(CONCURRENT_REQUESTS);
        assertThat(allocatedNumbers.first()).isEqualTo(2L);
        assertThat(allocatedNumbers.last()).isEqualTo((long) CONCURRENT_REQUESTS + 1L);
        assertThat(totalHeaders).isEqualTo(CONCURRENT_REQUESTS + 1);
        assertThat(distinctNumbers).isEqualTo(CONCURRENT_REQUESTS + 1);
    }
}
