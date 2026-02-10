package com.saulpos.server.giftcard;

import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.receipt.model.ReceiptHeaderEntity;
import com.saulpos.server.receipt.model.ReceiptSeriesEntity;
import com.saulpos.server.receipt.repository.ReceiptHeaderRepository;
import com.saulpos.server.receipt.repository.ReceiptSeriesRepository;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class GiftCardIntegrationTest {

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
    private CustomerRepository customerRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private SaleCartRepository saleCartRepository;

    @Autowired
    private ReceiptSeriesRepository receiptSeriesRepository;

    @Autowired
    private ReceiptHeaderRepository receiptHeaderRepository;

    @Autowired
    private SaleRepository saleRepository;

    private MerchantEntity merchant;
    private CustomerEntity customer;
    private Long saleId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM gift_card_transaction");
        jdbcTemplate.execute("DELETE FROM gift_card");
        jdbcTemplate.execute("DELETE FROM sale_return_line");
        jdbcTemplate.execute("DELETE FROM sale_return_refund");
        jdbcTemplate.execute("DELETE FROM sale_return");
        jdbcTemplate.execute("DELETE FROM sale_line");
        jdbcTemplate.execute("DELETE FROM sale");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart");
        jdbcTemplate.execute("DELETE FROM receipt_header");
        jdbcTemplate.execute("DELETE FROM receipt_sequence");
        jdbcTemplate.execute("DELETE FROM receipt_series");
        jdbcTemplate.execute("DELETE FROM customer_group_assignment");
        jdbcTemplate.execute("DELETE FROM customer_group");
        jdbcTemplate.execute("DELETE FROM customer_contact");
        jdbcTemplate.execute("DELETE FROM customer_tax_identity");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM terminal_device");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");
        jdbcTemplate.execute("DELETE FROM auth_audit_event");
        jdbcTemplate.execute("DELETE FROM auth_session");
        jdbcTemplate.execute("DELETE FROM user_role");
        jdbcTemplate.execute("DELETE FROM user_account");

        merchant = new MerchantEntity();
        merchant.setCode("MER-GC-INT");
        merchant.setName("Merchant GiftCard Integration");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-GC-INT");
        storeLocation.setName("Store GiftCard Integration");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-GC-INT");
        terminal.setName("Terminal GiftCard Integration");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);

        customer = new CustomerEntity();
        customer.setMerchant(merchant);
        customer.setDisplayName("Gift Card Customer");
        customer.setActive(true);
        customer = customerRepository.save(customer);

        UserAccountEntity user = new UserAccountEntity();
        user.setUsername("gift-card-cashier");
        user.setPasswordHash("hash");
        user.setActive(true);
        user = userAccountRepository.save(user);

        SaleCartEntity cart = new SaleCartEntity();
        cart.setCashierUser(user);
        cart.setStoreLocation(storeLocation);
        cart.setTerminalDevice(terminal);
        cart.setStatus(SaleCartStatus.CHECKED_OUT);
        cart.setPricingAt(Instant.now());
        cart.setSubtotalNet(new BigDecimal("10.00"));
        cart.setTotalTax(BigDecimal.ZERO);
        cart.setTotalGross(new BigDecimal("10.00"));
        cart.setRoundingAdjustment(BigDecimal.ZERO);
        cart.setTotalPayable(new BigDecimal("10.00"));
        cart = saleCartRepository.save(cart);

        ReceiptSeriesEntity series = new ReceiptSeriesEntity();
        series.setStoreLocation(storeLocation);
        series.setTerminalDevice(terminal);
        series.setSeriesCode("GCINT");
        series.setActive(true);
        series = receiptSeriesRepository.save(series);
        jdbcTemplate.update("INSERT INTO receipt_sequence(series_id, next_number, version) VALUES (?, ?, ?)",
                series.getId(), 2L, 0L);

        ReceiptHeaderEntity receiptHeader = new ReceiptHeaderEntity();
        receiptHeader.setSeries(series);
        receiptHeader.setStoreLocation(storeLocation);
        receiptHeader.setTerminalDevice(terminal);
        receiptHeader.setNumber(1L);
        receiptHeader.setReceiptNumber("GCINT-1");
        receiptHeader = receiptHeaderRepository.save(receiptHeader);

        SaleEntity sale = new SaleEntity();
        sale.setCart(cart);
        sale.setCashierUser(user);
        sale.setStoreLocation(storeLocation);
        sale.setTerminalDevice(terminal);
        sale.setCustomer(customer);
        sale.setReceiptHeaderId(receiptHeader.getId());
        sale.setReceiptNumber("GCINT-1");
        sale.setSubtotalNet(new BigDecimal("10.00"));
        sale.setTotalTax(BigDecimal.ZERO);
        sale.setTotalGross(new BigDecimal("10.00"));
        sale.setRoundingAdjustment(BigDecimal.ZERO);
        sale.setTotalPayable(new BigDecimal("10.00"));
        sale = saleRepository.save(sale);
        saleId = sale.getId();
    }

    @Test
    void issueAndRedeemGiftCardTracksSaleContextAndPreventsOverdraw() throws Exception {
        mockMvc.perform(post("/api/gift-cards/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "customerId": %d,
                                  "cardNumber": "GC-INT-001",
                                  "issuedAmount": 100.00,
                                  "note": "initial issue"
                                }
                                """.formatted(merchant.getId(), customer.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balanceAmount").value(100.00))
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].transactionType").value("ISSUE"));

        mockMvc.perform(post("/api/gift-cards/{cardNumber}/redeem", "GC-INT-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "amount": 35.50,
                                  "saleId": %d,
                                  "reference": "SALE-%d",
                                  "note": "checkout redemption"
                                }
                                """.formatted(merchant.getId(), saleId, saleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAmount").value(64.50))
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andExpect(jsonPath("$.transactions[1].transactionType").value("REDEEM"))
                .andExpect(jsonPath("$.transactions[1].saleId").value(saleId))
                .andExpect(jsonPath("$.transactions[1].saleReturnId").isEmpty());

        mockMvc.perform(post("/api/gift-cards/{cardNumber}/redeem", "GC-INT-001")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "amount": 80.00,
                                  "saleId": %d
                                }
                                """.formatted(merchant.getId(), saleId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        MvcResult currentCardResult = mockMvc.perform(get("/api/gift-cards/{cardNumber}", "GC-INT-001")
                        .param("merchantId", merchant.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andReturn();

        Integer redeemCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM gift_card_transaction WHERE transaction_type = 'REDEEM' AND sale_id = ?",
                Integer.class,
                saleId);
        assertThat(redeemCount).isEqualTo(1);
        assertThat(currentCardResult.getResponse().getContentAsString()).contains("\"balanceAmount\":64.50");
    }
}
