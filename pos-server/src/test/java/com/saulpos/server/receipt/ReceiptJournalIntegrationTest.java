package com.saulpos.server.receipt;

import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.sale.model.SaleCartEntity;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.repository.SaleCartRepository;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReceiptJournalIntegrationTest {

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

    @Autowired
    private SaleCartRepository saleCartRepository;

    @Autowired
    private SaleRepository saleRepository;

    private Long saleId;
    private String receiptNumber;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM receipt_print_event");
        jdbcTemplate.execute("DELETE FROM sale_return_refund");
        jdbcTemplate.execute("DELETE FROM sale_return_line");
        jdbcTemplate.execute("DELETE FROM sale_return");
        jdbcTemplate.execute("DELETE FROM payment_transition");
        jdbcTemplate.execute("DELETE FROM payment_allocation");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM inventory_movement");
        jdbcTemplate.execute("DELETE FROM sale_line");
        jdbcTemplate.execute("DELETE FROM sale");
        jdbcTemplate.execute("DELETE FROM sale_override_event");
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
        jdbcTemplate.execute("DELETE FROM receipt_header");
        jdbcTemplate.execute("DELETE FROM receipt_sequence");
        jdbcTemplate.execute("DELETE FROM receipt_series");
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
        cashier.setUsername("cashier-m4");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashier = userAccountRepository.save(cashier);

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-M4");
        merchant.setName("Merchant M4");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode("STORE-M4");
        store.setName("Store M4");
        store.setActive(true);
        store = storeLocationRepository.save(store);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode("TERM-M4");
        terminal.setName("Terminal M4");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);

        SaleCartEntity cart = new SaleCartEntity();
        cart.setCashierUser(cashier);
        cart.setStoreLocation(store);
        cart.setTerminalDevice(terminal);
        cart.setStatus(SaleCartStatus.CHECKED_OUT);
        cart.setPricingAt(Instant.parse("2026-02-10T12:00:00Z"));
        cart.setSubtotalNet(new BigDecimal("9.09"));
        cart.setTotalTax(new BigDecimal("0.91"));
        cart.setTotalGross(new BigDecimal("10.00"));
        cart.setRoundingAdjustment(BigDecimal.ZERO);
        cart.setTotalPayable(new BigDecimal("10.00"));
        cart = saleCartRepository.save(cart);

        SaleEntity sale = new SaleEntity();
        sale.setCart(cart);
        sale.setCashierUser(cashier);
        sale.setStoreLocation(store);
        sale.setTerminalDevice(terminal);
        sale.setReceiptHeaderId(1001L);
        sale.setReceiptNumber("RCPT-TERM-M4-00000001");
        sale.setSubtotalNet(new BigDecimal("9.09"));
        sale.setTotalTax(new BigDecimal("0.91"));
        sale.setTotalGross(new BigDecimal("10.00"));
        sale.setRoundingAdjustment(BigDecimal.ZERO);
        sale.setTotalPayable(new BigDecimal("10.00"));
        sale = saleRepository.save(sale);

        saleId = sale.getId();
        receiptNumber = sale.getReceiptNumber();
    }

    @Test
    @WithMockUser(username = "cashier-m4", authorities = {"PERM_SALES_PROCESS"})
    void journalBySaleIdReturnsReceiptDetails() throws Exception {
        mockMvc.perform(get("/api/receipts/journal/by-sale/{saleId}", saleId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").value(saleId))
                .andExpect(jsonPath("$.receiptNumber").value(receiptNumber))
                .andExpect(jsonPath("$.storeLocationCode").value("STORE-M4"))
                .andExpect(jsonPath("$.terminalCode").value("TERM-M4"))
                .andExpect(jsonPath("$.cashierUsername").value("cashier-m4"))
                .andExpect(jsonPath("$.totalPayable").value(10.00));
    }

    @Test
    @WithMockUser(username = "cashier-m4", authorities = {"PERM_SALES_PROCESS"})
    void journalByReceiptNumberReturnsReceiptDetails() throws Exception {
        mockMvc.perform(get("/api/receipts/journal/by-number/{receiptNumber}", receiptNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").value(saleId))
                .andExpect(jsonPath("$.receiptHeaderId").value(1001L))
                .andExpect(jsonPath("$.receiptNumber").value(receiptNumber));
    }

    @Test
    @WithMockUser(username = "refund-m4", authorities = {"PERM_REFUND_PROCESS"})
    void journalRequiresAuthorizedPermission() throws Exception {
        mockMvc.perform(get("/api/receipts/journal/by-sale/{saleId}", saleId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }
}
