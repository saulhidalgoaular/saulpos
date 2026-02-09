package com.saulpos.server.receipt;

import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
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
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class ReceiptAllocationIntegrationTest {

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

    private Long terminalDeviceId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
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
        merchant.setCode("MER-D3");
        merchant.setName("Merchant D3");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode("STORE-D3");
        store.setName("Store D3");
        store.setActive(true);
        store = storeLocationRepository.save(store);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode("term-d3-main");
        terminal.setName("Terminal D3 Main");
        terminal.setActive(true);
        terminalDeviceId = terminalDeviceRepository.save(terminal).getId();
    }

    @Test
    void allocateReturnsSequentialReceiptNumbers() throws Exception {
        mockMvc.perform(post("/api/receipts/allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terminalDeviceId": %d
                                }
                                """.formatted(terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numberPolicy").value("GAPLESS"))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.receiptNumber").value("RCPT-TERM-D3-MAIN-00000001"));

        mockMvc.perform(post("/api/receipts/allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terminalDeviceId": %d
                                }
                                """.formatted(terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(2))
                .andExpect(jsonPath("$.receiptNumber").value("RCPT-TERM-D3-MAIN-00000002"));

        Integer totalHeaders = jdbcTemplate.queryForObject("SELECT count(*) FROM receipt_header", Integer.class);
        Integer distinctNumbers = jdbcTemplate.queryForObject("SELECT count(DISTINCT number) FROM receipt_header", Integer.class);

        assertThat(totalHeaders).isEqualTo(2);
        assertThat(distinctNumbers).isEqualTo(2);
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"PERM_CONFIGURATION_MANAGE"})
    void allocateRequiresSalesPermission() throws Exception {
        mockMvc.perform(post("/api/receipts/allocate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "terminalDeviceId": %d
                                }
                                """.formatted(terminalDeviceId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }
}
