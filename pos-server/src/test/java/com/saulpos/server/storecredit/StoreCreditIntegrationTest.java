package com.saulpos.server.storecredit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.api.sale.PaymentStatus;
import com.saulpos.api.sale.SaleCartStatus;
import com.saulpos.api.tax.TenderType;
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
import com.saulpos.server.sale.model.PaymentEntity;
import com.saulpos.server.sale.model.SaleCartEntity;
import com.saulpos.server.sale.model.SaleEntity;
import com.saulpos.server.sale.model.SaleReturnEntity;
import com.saulpos.server.sale.repository.PaymentRepository;
import com.saulpos.server.sale.repository.SaleCartRepository;
import com.saulpos.server.sale.repository.SaleRepository;
import com.saulpos.server.sale.repository.SaleReturnRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
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
class StoreCreditIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private SaleReturnRepository saleReturnRepository;

    private MerchantEntity merchant;
    private CustomerEntity customer;
    private Long saleId;
    private Long saleReturnId;

    @BeforeEach
    void setUp() {
        cleanupData();
        seedData();
    }

    @AfterEach
    void tearDown() {
        cleanupData();
    }

    private void cleanupData() {
        jdbcTemplate.execute("DELETE FROM store_credit_transaction");
        jdbcTemplate.execute("DELETE FROM store_credit_account");
        jdbcTemplate.execute("DELETE FROM gift_card_transaction");
        jdbcTemplate.execute("DELETE FROM gift_card");
        jdbcTemplate.execute("DELETE FROM receipt_print_event");
        jdbcTemplate.execute("DELETE FROM no_sale_drawer_event");
        jdbcTemplate.execute("DELETE FROM inventory_movement_lot");
        jdbcTemplate.execute("DELETE FROM inventory_lot_balance");
        jdbcTemplate.execute("DELETE FROM inventory_lot");
        jdbcTemplate.execute("DELETE FROM inventory_movement");
        jdbcTemplate.execute("DELETE FROM cash_movement");
        jdbcTemplate.execute("DELETE FROM cash_shift");
        jdbcTemplate.execute("DELETE FROM sale_return_line");
        jdbcTemplate.execute("DELETE FROM sale_return_refund");
        jdbcTemplate.execute("DELETE FROM sale_return");
        jdbcTemplate.execute("DELETE FROM payment_transition");
        jdbcTemplate.execute("DELETE FROM payment_allocation");
        jdbcTemplate.execute("DELETE FROM payment");
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
    }

    private void seedData() {
        merchant = new MerchantEntity();
        merchant.setCode("MER-SC-INT");
        merchant.setName("Merchant StoreCredit Integration");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-SC-INT");
        storeLocation.setName("Store StoreCredit Integration");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-SC-INT");
        terminal.setName("Terminal StoreCredit Integration");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);

        customer = new CustomerEntity();
        customer.setMerchant(merchant);
        customer.setDisplayName("Store Credit Customer");
        customer.setActive(true);
        customer = customerRepository.save(customer);

        UserAccountEntity user = new UserAccountEntity();
        user.setUsername("store-credit-cashier");
        user.setPasswordHash("hash");
        user.setActive(true);
        user = userAccountRepository.save(user);

        SaleCartEntity cart = new SaleCartEntity();
        cart.setCashierUser(user);
        cart.setStoreLocation(storeLocation);
        cart.setTerminalDevice(terminal);
        cart.setStatus(SaleCartStatus.CHECKED_OUT);
        cart.setPricingAt(Instant.now());
        cart.setSubtotalNet(new BigDecimal("40.00"));
        cart.setTotalTax(BigDecimal.ZERO);
        cart.setTotalGross(new BigDecimal("40.00"));
        cart.setRoundingAdjustment(BigDecimal.ZERO);
        cart.setTotalPayable(new BigDecimal("40.00"));
        cart = saleCartRepository.save(cart);

        ReceiptSeriesEntity series = new ReceiptSeriesEntity();
        series.setStoreLocation(storeLocation);
        series.setTerminalDevice(terminal);
        series.setSeriesCode("SCINT");
        series.setActive(true);
        series = receiptSeriesRepository.save(series);
        jdbcTemplate.update("INSERT INTO receipt_sequence(series_id, next_number, version) VALUES (?, ?, ?)",
                series.getId(), 2L, 0L);

        ReceiptHeaderEntity receiptHeader = new ReceiptHeaderEntity();
        receiptHeader.setSeries(series);
        receiptHeader.setStoreLocation(storeLocation);
        receiptHeader.setTerminalDevice(terminal);
        receiptHeader.setNumber(1L);
        receiptHeader.setReceiptNumber("SCINT-1");
        receiptHeader = receiptHeaderRepository.save(receiptHeader);

        SaleEntity sale = new SaleEntity();
        sale.setCart(cart);
        sale.setCashierUser(user);
        sale.setStoreLocation(storeLocation);
        sale.setTerminalDevice(terminal);
        sale.setCustomer(customer);
        sale.setReceiptHeaderId(receiptHeader.getId());
        sale.setReceiptNumber("SCINT-1");
        sale.setSubtotalNet(new BigDecimal("40.00"));
        sale.setTotalTax(BigDecimal.ZERO);
        sale.setTotalGross(new BigDecimal("40.00"));
        sale.setRoundingAdjustment(BigDecimal.ZERO);
        sale.setTotalPayable(new BigDecimal("40.00"));
        sale = saleRepository.save(sale);
        saleId = sale.getId();

        PaymentEntity payment = new PaymentEntity();
        payment.setCart(cart);
        payment.setTotalPayable(new BigDecimal("40.00"));
        payment.setTotalAllocated(new BigDecimal("40.00"));
        payment.setTotalTendered(new BigDecimal("40.00"));
        payment.setChangeAmount(BigDecimal.ZERO);
        payment.setStatus(PaymentStatus.CAPTURED);
        payment = paymentRepository.save(payment);

        SaleReturnEntity saleReturn = new SaleReturnEntity();
        saleReturn.setSale(sale);
        saleReturn.setPayment(payment);
        saleReturn.setReasonCode("DAMAGED");
        saleReturn.setRefundTenderType(TenderType.CASH);
        saleReturn.setRefundNote("damaged item");
        saleReturn.setReturnReference("RET-SC-1");
        saleReturn.setSubtotalNet(new BigDecimal("18.00"));
        saleReturn.setTotalTax(BigDecimal.ZERO);
        saleReturn.setTotalGross(new BigDecimal("18.00"));
        saleReturn = saleReturnRepository.save(saleReturn);
        saleReturnId = saleReturn.getId();
    }

    @Test
    void issueAndRedeemStoreCreditTracksRefundAndSaleContexts() throws Exception {
        MvcResult issueResult = mockMvc.perform(post("/api/store-credits/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "customerId": %d,
                                  "amount": 18.00,
                                  "saleReturnId": %d,
                                  "reference": "RET-%d",
                                  "note": "refund as credit"
                                }
                                """.formatted(merchant.getId(), customer.getId(), saleReturnId, saleReturnId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balanceAmount").value(18.00))
                .andExpect(jsonPath("$.transactions.length()").value(1))
                .andExpect(jsonPath("$.transactions[0].transactionType").value("ISSUE"))
                .andExpect(jsonPath("$.transactions[0].saleReturnId").value(saleReturnId))
                .andReturn();

        JsonNode issuePayload = objectMapper.readTree(issueResult.getResponse().getContentAsString());
        long accountId = issuePayload.get("id").asLong();

        mockMvc.perform(post("/api/store-credits/{accountId}/redeem", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "amount": 7.25,
                                  "saleId": %d,
                                  "reference": "SALE-%d",
                                  "note": "redeem in new sale"
                                }
                                """.formatted(merchant.getId(), saleId, saleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balanceAmount").value(10.75))
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andExpect(jsonPath("$.transactions[1].transactionType").value("REDEEM"))
                .andExpect(jsonPath("$.transactions[1].saleId").value(saleId));

        mockMvc.perform(post("/api/store-credits/{accountId}/redeem", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "amount": 20.00,
                                  "saleId": %d
                                }
                                """.formatted(merchant.getId(), saleId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(get("/api/store-credits/{accountId}", accountId)
                        .param("merchantId", merchant.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions.length()").value(2))
                .andExpect(jsonPath("$.balanceAmount").value(10.75));

        Integer issueCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM store_credit_transaction WHERE transaction_type = 'ISSUE' AND sale_return_id = ?",
                Integer.class,
                saleReturnId);
        Integer redeemCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM store_credit_transaction WHERE transaction_type = 'REDEEM' AND sale_id = ?",
                Integer.class,
                saleId);

        assertThat(issueCount).isEqualTo(1);
        assertThat(redeemCount).isEqualTo(1);
    }

    @Test
    void issueRejectsMismatchedSaleReturnCustomerContext() throws Exception {
        CustomerEntity otherCustomer = new CustomerEntity();
        otherCustomer.setMerchant(merchant);
        otherCustomer.setDisplayName("Other Customer");
        otherCustomer.setActive(true);
        otherCustomer = customerRepository.save(otherCustomer);

        mockMvc.perform(post("/api/store-credits/issue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "customerId": %d,
                                  "amount": 18.00,
                                  "saleReturnId": %d
                                }
                                """.formatted(merchant.getId(), otherCustomer.getId(), saleReturnId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));
    }
}
