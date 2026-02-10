package com.saulpos.server.sale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.api.tax.TaxMode;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import com.saulpos.server.tax.model.StoreTaxRuleEntity;
import com.saulpos.server.tax.model.TaxGroupEntity;
import com.saulpos.server.tax.repository.StoreTaxRuleRepository;
import com.saulpos.server.tax.repository.TaxGroupRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS", "PERM_REFUND_PROCESS"})
class PaymentStateMachineIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TaxGroupRepository taxGroupRepository;

    @Autowired
    private StoreTaxRuleRepository storeTaxRuleRepository;

    private Long cashierUserId;
    private Long storeLocationId;
    private Long terminalDeviceId;
    private Long productId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM idempotency_key_event");
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
        jdbcTemplate.execute("DELETE FROM customer_group_assignment");
        jdbcTemplate.execute("DELETE FROM customer_group");
        jdbcTemplate.execute("DELETE FROM customer_contact");
        jdbcTemplate.execute("DELETE FROM customer_tax_identity");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM promotion_window");
        jdbcTemplate.execute("DELETE FROM promotion_rule");
        jdbcTemplate.execute("DELETE FROM promotion");
        jdbcTemplate.execute("DELETE FROM discount_application");
        jdbcTemplate.execute("DELETE FROM discount_reason_code");
        jdbcTemplate.execute("DELETE FROM receipt_header");
        jdbcTemplate.execute("DELETE FROM receipt_sequence");
        jdbcTemplate.execute("DELETE FROM receipt_series");
        jdbcTemplate.execute("DELETE FROM rounding_policy");
        jdbcTemplate.execute("DELETE FROM store_tax_rule");
        jdbcTemplate.execute("DELETE FROM tax_group");
        jdbcTemplate.execute("DELETE FROM open_price_entry_audit");
        jdbcTemplate.execute("DELETE FROM store_price_override");
        jdbcTemplate.execute("DELETE FROM price_book_item");
        jdbcTemplate.execute("DELETE FROM price_book");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM product");
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

        UserAccountEntity cashier = new UserAccountEntity();
        cashier.setUsername("cashier-user-j2");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashierUserId = userAccountRepository.save(cashier).getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-J2");
        merchant.setName("Merchant J2");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-J2");
        storeLocation.setName("Store J2");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-J2");
        terminal.setName("Terminal J2");
        terminal.setActive(true);
        terminalDeviceId = terminalDeviceRepository.save(terminal).getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-J2");
        category.setName("Category J2");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10-J2");
        taxGroup.setName("VAT 10 J2");
        taxGroup.setTaxRatePercent(new BigDecimal("10.0000"));
        taxGroup.setZeroRated(false);
        taxGroup.setActive(true);
        taxGroup = taxGroupRepository.save(taxGroup);

        StoreTaxRuleEntity taxRule = new StoreTaxRuleEntity();
        taxRule.setStoreLocation(storeLocation);
        taxRule.setTaxGroup(taxGroup);
        taxRule.setTaxMode(TaxMode.EXCLUSIVE);
        taxRule.setExempt(false);
        taxRule.setActive(true);
        storeTaxRuleRepository.save(taxRule);

        ProductEntity product = new ProductEntity();
        product.setMerchant(merchant);
        product.setCategory(category);
        product.setTaxGroup(taxGroup);
        product.setSku("SKU-J2-001");
        product.setName("Product J2");
        product.setBasePrice(new BigDecimal("5.00"));
        product.setDescription("Payment lifecycle item");
        product.setActive(true);
        productId = productRepository.save(product).getId();
    }

    @Test
    void paymentLifecycleSupportsAuthorizeCaptureAndRefundWithTransitionHistory() throws Exception {
        long cartId = createCart();
        addLine(cartId);
        long paymentId = checkout(cartId);

        mockMvc.perform(get("/api/payments/{id}", paymentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value(paymentId))
                .andExpect(jsonPath("$.status").value("AUTHORIZED"))
                .andExpect(jsonPath("$.transitions.length()").value(1))
                .andExpect(jsonPath("$.transitions[0].action").value("AUTHORIZE"))
                .andExpect(jsonPath("$.transitions[0].toStatus").value("AUTHORIZED"));

        mockMvc.perform(post("/api/payments/{id}/capture", paymentId)
                        .header("Idempotency-Key", "payment-capture-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "capture at terminal close"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.transitions.length()").value(2))
                .andExpect(jsonPath("$.transitions[1].action").value("CAPTURE"))
                .andExpect(jsonPath("$.transitions[1].fromStatus").value("AUTHORIZED"))
                .andExpect(jsonPath("$.transitions[1].toStatus").value("CAPTURED"));

        mockMvc.perform(post("/api/payments/{id}/refund", paymentId)
                        .header("Idempotency-Key", "payment-refund-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "customer refund approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REFUNDED"))
                .andExpect(jsonPath("$.transitions.length()").value(3))
                .andExpect(jsonPath("$.transitions[2].action").value("REFUND"))
                .andExpect(jsonPath("$.transitions[2].fromStatus").value("CAPTURED"))
                .andExpect(jsonPath("$.transitions[2].toStatus").value("REFUNDED"));

        String statusValue = jdbcTemplate.queryForObject(
                "SELECT status FROM payment WHERE id = ?",
                String.class,
                paymentId);
        Integer transitionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment_transition WHERE payment_id = ?",
                Integer.class,
                paymentId);

        assertThat(statusValue).isEqualTo("REFUNDED");
        assertThat(transitionCount).isEqualTo(3);
    }

    @Test
    void invalidTransitionIsRejectedWithConflict() throws Exception {
        long cartId = createCart();
        addLine(cartId);
        long paymentId = checkout(cartId);

        mockMvc.perform(post("/api/payments/{id}/capture", paymentId)
                        .header("Idempotency-Key", "payment-capture-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "capture payment"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"));

        mockMvc.perform(post("/api/payments/{id}/void", paymentId)
                        .header("Idempotency-Key", "payment-void-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "void after capture should fail"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));
    }

    @Test
    void checkoutReplayWithSameIdempotencyKeyReturnsOriginalResponse() throws Exception {
        long cartId = createCart();
        addLine(cartId);
        String idempotencyKey = "checkout-replay-" + UUID.randomUUID();
        String requestBody = """
                {
                  "cartId": %d,
                  "cashierUserId": %d,
                  "terminalDeviceId": %d,
                  "payments": [
                    {
                      "tenderType": "CASH",
                      "amount": 11.00,
                      "tenderedAmount": 11.00
                    }
                  ]
                }
                """.formatted(cartId, cashierUserId, terminalDeviceId);

        MvcResult firstResult = mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult replayResult = mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode firstJson = objectMapper.readTree(firstResult.getResponse().getContentAsString());
        JsonNode replayJson = objectMapper.readTree(replayResult.getResponse().getContentAsString());

        Integer saleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sale WHERE cart_id = ?",
                Integer.class,
                cartId);
        Integer paymentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment WHERE cart_id = ?",
                Integer.class,
                cartId);

        assertThat(replayJson.get("saleId").asLong()).isEqualTo(firstJson.get("saleId").asLong());
        assertThat(replayJson.get("paymentId").asLong()).isEqualTo(firstJson.get("paymentId").asLong());
        assertThat(replayJson.get("receiptNumber").asText()).isEqualTo(firstJson.get("receiptNumber").asText());
        assertThat(saleCount).isEqualTo(1);
        assertThat(paymentCount).isEqualTo(1);
    }

    @Test
    void checkoutReuseWithDifferentPayloadReturnsConflict() throws Exception {
        long cartId = createCart();
        addLine(cartId);
        String idempotencyKey = "checkout-conflict-" + UUID.randomUUID();

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CASH",
                                      "amount": 11.00,
                                      "tenderedAmount": 11.00
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CARD",
                                      "amount": 11.00,
                                      "reference": "ALT-REF"
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));
    }

    @Test
    void paymentTransitionReplayReturnsOriginalAndPayloadMismatchConflicts() throws Exception {
        long cartId = createCart();
        addLine(cartId);
        long paymentId = checkout(cartId);
        String idempotencyKey = "capture-replay-" + UUID.randomUUID();

        mockMvc.perform(post("/api/payments/{id}/capture", paymentId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "capture once"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.transitions.length()").value(2));

        mockMvc.perform(post("/api/payments/{id}/capture", paymentId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "capture once"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CAPTURED"))
                .andExpect(jsonPath("$.transitions.length()").value(2));

        Integer captureTransitions = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment_transition WHERE payment_id = ? AND action = 'CAPTURE'",
                Integer.class,
                paymentId);
        assertThat(captureTransitions).isEqualTo(1);

        mockMvc.perform(post("/api/payments/{id}/capture", paymentId)
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "different note"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));
    }

    private long createCart() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sales/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "storeLocationId": %d,
                                  "terminalDeviceId": %d,
                                  "pricingAt": "%s"
                                }
                                """.formatted(
                                cashierUserId,
                                storeLocationId,
                                terminalDeviceId,
                                Instant.parse("2026-02-10T12:00:00Z"))))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private void addLine(long cartId) throws Exception {
        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "scan-j2-1",
                                  "productId": %d,
                                  "quantity": 2
                                }
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayable").value(11.00));
    }

    private long checkout(long cartId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "payment-checkout-" + cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CASH",
                                      "amount": 11.00,
                                      "tenderedAmount": 11.00
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentStatus").value("AUTHORIZED"))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("paymentId").asLong();
    }
}
