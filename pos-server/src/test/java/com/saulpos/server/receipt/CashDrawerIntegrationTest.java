package com.saulpos.server.receipt;

import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CashDrawerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private StoreLocationRepository storeLocationRepository;

    @Autowired
    private TerminalDeviceRepository terminalDeviceRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    private Long terminalDeviceId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM no_sale_drawer_event");
        jdbcTemplate.execute("DELETE FROM receipt_header");
        jdbcTemplate.execute("DELETE FROM receipt_sequence");
        jdbcTemplate.execute("DELETE FROM receipt_series");
        jdbcTemplate.execute("DELETE FROM inventory_movement");
        jdbcTemplate.execute("DELETE FROM sale_line");
        jdbcTemplate.execute("DELETE FROM sale");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
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
        cashier.setUsername("cashier-drawer");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        userAccountRepository.save(cashier);

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-M2");
        merchant.setName("Merchant M2");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode("STORE-M2");
        store.setName("Store M2");
        store.setActive(true);
        store = storeLocationRepository.save(store);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode("term-m2-main");
        terminal.setName("Terminal M2 Main");
        terminal.setActive(true);
        terminalDeviceId = terminalDeviceRepository.save(terminal).getId();
    }

    @Test
    @WithMockUser(username = "cashier-drawer", authorities = {"PERM_CASH_DRAWER_OPEN"})
    void openDrawerPersistsNoSaleAuditEvent() throws Exception {
        mockMvc.perform(post("/api/receipts/drawer/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-ID", "corr-m2-001")
                        .content("""
                                {
                                  "terminalDeviceId": %d,
                                  "reasonCode": "no_sale",
                                  "note": "manual drawer access",
                                  "referenceNumber": "NO-SALE-M2-1"
                                }
                                """.formatted(terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.adapter").value("ESC_POS"))
                .andExpect(jsonPath("$.terminalDeviceId").value(terminalDeviceId))
                .andExpect(jsonPath("$.terminalCode").value("term-m2-main"));

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM no_sale_drawer_event", Integer.class);
        String reasonCode = jdbcTemplate.queryForObject("SELECT reason_code FROM no_sale_drawer_event LIMIT 1", String.class);
        String correlationId = jdbcTemplate.queryForObject("SELECT correlation_id FROM no_sale_drawer_event LIMIT 1", String.class);
        String actorUsername = jdbcTemplate.queryForObject("SELECT actor_username FROM no_sale_drawer_event LIMIT 1", String.class);

        assertThat(count).isEqualTo(1);
        assertThat(reasonCode).isEqualTo("NO_SALE");
        assertThat(correlationId).isEqualTo("corr-m2-001");
        assertThat(actorUsername).isEqualTo("cashier-drawer");
    }

    @Test
    @WithMockUser(username = "cashier-drawer", authorities = {"PERM_SALES_PROCESS"})
    void openDrawerRequiresCashDrawerPermission() throws Exception {
        mockMvc.perform(post("/api/receipts/drawer/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terminalDeviceId": %d,
                                  "reasonCode": "NO_SALE"
                                }
                                """.formatted(terminalDeviceId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }
}
