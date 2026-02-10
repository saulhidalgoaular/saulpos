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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class SaleCartIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    private Long unitProductId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM payment_allocation");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM sale_override_event");
        jdbcTemplate.execute("DELETE FROM void_reason_code");
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
        jdbcTemplate.execute("DELETE FROM inventory_movement");
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
        cashier.setUsername("cashier-user");
        cashier.setPasswordHash(passwordEncoder.encode("Pass123!"));
        cashier.setActive(true);
        cashier = userAccountRepository.save(cashier);
        cashierUserId = cashier.getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-G1");
        merchant.setName("Merchant G1");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);
        seedVoidReasonCodes();

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-G1");
        storeLocation.setName("Store G1");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-G1");
        terminal.setName("Terminal G1");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);
        terminalDeviceId = terminal.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-G1");
        category.setName("Category G1");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10");
        taxGroup.setName("VAT 10");
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
        product.setSku("SKU-G1-001");
        product.setName("Product G1");
        product.setBasePrice(new BigDecimal("5.00"));
        product.setDescription("Cart item");
        product.setActive(true);
        product = productRepository.save(product);
        unitProductId = product.getId();
    }

    private void seedVoidReasonCodes() {
        jdbcTemplate.update(
                "INSERT INTO void_reason_code (merchant_id, code, description, is_active, created_at, updated_at) "
                        + "VALUES (NULL, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "SCAN_ERROR",
                "Scan error");
        jdbcTemplate.update(
                "INSERT INTO void_reason_code (merchant_id, code, description, is_active, created_at, updated_at) "
                        + "VALUES (NULL, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "PRICE_MATCH",
                "Price match");
        jdbcTemplate.update(
                "INSERT INTO void_reason_code (merchant_id, code, description, is_active, created_at, updated_at) "
                        + "VALUES (NULL, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "CUSTOMER_REQUEST",
                "Customer request");
    }

    @Test
    void cartLifecycleSupportsDeterministicMutationAndRecalculation() throws Exception {
        long cartId = createCart();

        MvcResult addResult = mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "scan-1",
                                  "productId": %d,
                                  "quantity": 2
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(1))
                .andExpect(jsonPath("$.subtotalNet").value(10.00))
                .andExpect(jsonPath("$.totalTax").value(1.00))
                .andExpect(jsonPath("$.totalGross").value(11.00))
                .andExpect(jsonPath("$.totalPayable").value(11.00))
                .andReturn();

        JsonNode addJson = objectMapper.readTree(addResult.getResponse().getContentAsString());
        long lineId = addJson.get("lines").get(0).get("lineId").asLong();

        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "scan-1",
                                  "productId": %d,
                                  "quantity": 2
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(1))
                .andExpect(jsonPath("$.lines[0].lineId").value(lineId))
                .andExpect(jsonPath("$.subtotalNet").value(10.00));

        mockMvc.perform(put("/api/sales/carts/{id}/lines/{lineId}", cartId, lineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "quantity": 3
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].quantity").value(3.000))
                .andExpect(jsonPath("$.subtotalNet").value(15.00))
                .andExpect(jsonPath("$.totalTax").value(1.50))
                .andExpect(jsonPath("$.totalGross").value(16.50));

        mockMvc.perform(post("/api/sales/carts/{id}/recalculate", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tenderType": "CASH"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotalNet").value(15.00))
                .andExpect(jsonPath("$.totalTax").value(1.50))
                .andExpect(jsonPath("$.totalGross").value(16.50))
                .andExpect(jsonPath("$.totalPayable").value(16.50));

        mockMvc.perform(get("/api/sales/carts/{id}", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(1))
                .andExpect(jsonPath("$.subtotalNet").value(15.00))
                .andExpect(jsonPath("$.totalTax").value(1.50))
                .andExpect(jsonPath("$.totalGross").value(16.50));

        mockMvc.perform(delete("/api/sales/carts/{id}/lines/{lineId}", cartId, lineId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(0))
                .andExpect(jsonPath("$.subtotalNet").value(0.00))
                .andExpect(jsonPath("$.totalTax").value(0.00))
                .andExpect(jsonPath("$.totalGross").value(0.00))
                .andExpect(jsonPath("$.totalPayable").value(0.00));
    }

    @Test
    void checkoutAcceptsSplitPaymentsAndPersistsPaymentAllocations() throws Exception {
        long cartId = createCart();

        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "scan-checkout-1",
                                  "productId": %d,
                                  "quantity": 2
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayable").value(11.00));

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "checkout-split-payments-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CARD",
                                      "amount": 6.00,
                                      "reference": "AUTH-123"
                                    },
                                    {
                                      "tenderType": "CASH",
                                      "amount": 5.00,
                                      "tenderedAmount": 10.00
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(cartId))
                .andExpect(jsonPath("$.saleId").isNumber())
                .andExpect(jsonPath("$.receiptNumber").isString())
                .andExpect(jsonPath("$.totalPayable").value(11.00))
                .andExpect(jsonPath("$.totalAllocated").value(11.00))
                .andExpect(jsonPath("$.totalTendered").value(16.00))
                .andExpect(jsonPath("$.changeAmount").value(5.00))
                .andExpect(jsonPath("$.payments.length()").value(2))
                .andExpect(jsonPath("$.payments[0].tenderType").value("CARD"))
                .andExpect(jsonPath("$.payments[0].amount").value(6.00))
                .andExpect(jsonPath("$.payments[0].tenderedAmount").value(6.00))
                .andExpect(jsonPath("$.payments[0].changeAmount").value(0.00))
                .andExpect(jsonPath("$.payments[1].tenderType").value("CASH"))
                .andExpect(jsonPath("$.payments[1].amount").value(5.00))
                .andExpect(jsonPath("$.payments[1].tenderedAmount").value(10.00))
                .andExpect(jsonPath("$.payments[1].changeAmount").value(5.00));

        Integer paymentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM payment WHERE cart_id = ?",
                Integer.class,
                cartId);
        assertThat(paymentCount).isEqualTo(1);

        Integer saleCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sale WHERE cart_id = ?",
                Integer.class,
                cartId);
        assertThat(saleCount).isEqualTo(1);

        Integer saleLineCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sale_line sl "
                        + "JOIN sale s ON s.id = sl.sale_id "
                        + "WHERE s.cart_id = ?",
                Integer.class,
                cartId);
        assertThat(saleLineCount).isEqualTo(1);

        Integer movementCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM inventory_movement im "
                        + "JOIN sale s ON s.id = im.sale_id "
                        + "WHERE s.cart_id = ? AND im.movement_type = 'SALE'",
                Integer.class,
                cartId);
        assertThat(movementCount).isEqualTo(1);

        String cartStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM sale_cart WHERE id = ?",
                String.class,
                cartId);
        assertThat(cartStatus).isEqualTo("CHECKED_OUT");

        List<String> tenderTypes = jdbcTemplate.queryForList(
                "SELECT tender_type FROM payment_allocation pa "
                        + "JOIN payment p ON p.id = pa.payment_id "
                        + "WHERE p.cart_id = ? ORDER BY pa.sequence_number",
                String.class,
                cartId);
        assertThat(tenderTypes).containsExactly("CARD", "CASH");
    }

    @Test
    void checkoutRejectsAllocationWhenTotalDoesNotMatchPayable() throws Exception {
        long cartId = createCart();

        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 2
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayable").value(11.00));

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "checkout-invalid-allocation-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CARD",
                                      "amount": 4.00
                                    },
                                    {
                                      "tenderType": "CASH",
                                      "amount": 5.00
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    @Test
    void lineVoidAndPriceOverrideRecomputeTotalsAndPersistAuditTrail() throws Exception {
        long cartId = createCart();

        MvcResult addResult = mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "scan-override-1",
                                  "productId": %d,
                                  "quantity": 2
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotalNet").value(10.00))
                .andReturn();
        long lineId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .path("lines")
                .get(0)
                .path("lineId")
                .asLong();

        mockMvc.perform(post("/api/sales/carts/{id}/lines/{lineId}/price-override", cartId, lineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "unitPrice": 4.50,
                                  "reasonCode": "PRICE_MATCH",
                                  "note": "local competitor match"
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotalNet").value(9.00))
                .andExpect(jsonPath("$.totalTax").value(0.90))
                .andExpect(jsonPath("$.totalGross").value(9.90));

        mockMvc.perform(post("/api/sales/carts/{id}/lines/{lineId}/void", cartId, lineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "reasonCode": "SCAN_ERROR",
                                  "note": "duplicate scan"
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(0))
                .andExpect(jsonPath("$.subtotalNet").value(0.00))
                .andExpect(jsonPath("$.totalTax").value(0.00))
                .andExpect(jsonPath("$.totalGross").value(0.00));

        List<String> overrideEvents = jdbcTemplate.queryForList(
                "SELECT event_type FROM sale_override_event WHERE cart_id = ? ORDER BY id",
                String.class,
                cartId);
        assertThat(overrideEvents).containsExactly("PRICE_OVERRIDE", "LINE_VOID");
    }

    @Test
    void priceOverrideAboveThresholdRequiresManagerApprovalPermission() throws Exception {
        long cartId = createCart();

        MvcResult addResult = mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isOk())
                .andReturn();
        long lineId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .path("lines")
                .get(0)
                .path("lineId")
                .asLong();

        mockMvc.perform(post("/api/sales/carts/{id}/lines/{lineId}/price-override", cartId, lineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "unitPrice": 1.00,
                                  "reasonCode": "PRICE_MATCH",
                                  "note": "aggressive markdown"
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"PERM_SALES_PROCESS", "PERM_DISCOUNT_OVERRIDE"})
    void managerPermissionAllowsHighThresholdPriceOverride() throws Exception {
        long cartId = createCart();

        MvcResult addResult = mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isOk())
                .andReturn();
        long lineId = objectMapper.readTree(addResult.getResponse().getContentAsString())
                .path("lines")
                .get(0)
                .path("lineId")
                .asLong();

        mockMvc.perform(post("/api/sales/carts/{id}/lines/{lineId}/price-override", cartId, lineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "unitPrice": 1.00,
                                  "reasonCode": "PRICE_MATCH"
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].unitPrice").value(1.00));

        Integer approvalRequiredCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sale_override_event WHERE cart_id = ? AND approval_required = TRUE",
                Integer.class,
                cartId);
        assertThat(approvalRequiredCount).isEqualTo(1);
    }

    @Test
    void cartVoidEndpointCancelsCartAndWritesOverrideAuditEvent() throws Exception {
        long cartId = createCart();

        mockMvc.perform(post("/api/sales/carts/{id}/void", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "reasonCode": "CUSTOMER_REQUEST",
                                  "note": "customer changed mind"
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        List<String> overrideEvents = jdbcTemplate.queryForList(
                "SELECT event_type FROM sale_override_event WHERE cart_id = ? ORDER BY id",
                String.class,
                cartId);
        assertThat(overrideEvents).containsExactly("CART_VOID");
    }

    @Test
    void addingLineRejectsInvalidQuantityForUnitProduct() throws Exception {
        long cartId = createCart();

        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1.250
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    @Test
    void addingLineRejectsUnknownProduct() throws Exception {
        long cartId = createCart();

        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": 999999,
                                  "quantity": 1
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));
    }

    @Test
    void parkedCartLifecycleSupportsParkResumeCancelAndAuditEvents() throws Exception {
        long cartId = createCart();

        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "scan-park-1",
                                  "productId": %d,
                                  "quantity": 2
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines.length()").value(1));

        mockMvc.perform(post("/api/sales/carts/{id}/park", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "note": "customer requested hold"
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARKED"));

        mockMvc.perform(post("/api/sales/carts/{id}/resume", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.lines.length()").value(1))
                .andExpect(jsonPath("$.totalPayable").value(11.00));

        mockMvc.perform(post("/api/sales/carts/{id}/cancel", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "reason": "customer abandoned checkout"
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        List<String> events = jdbcTemplate.queryForList(
                "SELECT event_type FROM sale_cart_event WHERE cart_id = ? ORDER BY id",
                String.class,
                cartId);

        assertThat(events).containsExactly("PARKED", "RESUMED", "CANCELLED");
    }

    @Test
    void listParkedCartsSupportsStoreAndTerminalFilters() throws Exception {
        long firstCartId = createCart();
        mockMvc.perform(post("/api/sales/carts/{id}/park", firstCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk());

        TerminalDeviceEntity secondTerminal = new TerminalDeviceEntity();
        secondTerminal.setStoreLocation(storeLocationRepository.findById(storeLocationId).orElseThrow());
        secondTerminal.setCode("TERM-G1-B");
        secondTerminal.setName("Terminal G1 B");
        secondTerminal.setActive(true);
        secondTerminal = terminalDeviceRepository.save(secondTerminal);

        long secondCartId = createCart(secondTerminal.getId());
        mockMvc.perform(post("/api/sales/carts/{id}/park", secondCartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d
                                }
                                """.formatted(cashierUserId, secondTerminal.getId())))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/sales/carts/parked")
                        .param("storeLocationId", String.valueOf(storeLocationId))
                        .param("terminalDeviceId", String.valueOf(terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].cartId").value(firstCartId))
                .andExpect(jsonPath("$[0].terminalDeviceId").value(terminalDeviceId));

        mockMvc.perform(get("/api/sales/carts/parked")
                        .param("storeLocationId", String.valueOf(storeLocationId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void resumeRejectsExpiredParkedCart() throws Exception {
        long cartId = createCart();

        mockMvc.perform(post("/api/sales/carts/{id}/park", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARKED"));

        jdbcTemplate.update(
                "UPDATE parked_cart_reference "
                        + "SET parked_at = DATEADD('MINUTE', -10, CURRENT_TIMESTAMP), "
                        + "expires_at = DATEADD('MINUTE', -5, CURRENT_TIMESTAMP) "
                        + "WHERE cart_id = ?",
                cartId);

        mockMvc.perform(post("/api/sales/carts/{id}/resume", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d
                                }
                                """.formatted(cashierUserId, terminalDeviceId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(get("/api/sales/carts/{id}", cartId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EXPIRED"));
    }

    @Test
    @WithMockUser(username = "reporter", authorities = {"PERM_REPORT_VIEW"})
    void parkResumeListAndCancelEndpointsRequireSalesPermission() throws Exception {
        mockMvc.perform(post("/api/sales/carts/{id}/park", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/carts/{id}/resume", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/carts/{id}/cancel", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1,
                                  "reason": "void"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/sales/carts/parked")
                        .param("storeLocationId", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/carts/{id}/void", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1,
                                  "reasonCode": "SCAN_ERROR"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/carts/{id}/lines/{lineId}/void", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1,
                                  "reasonCode": "SCAN_ERROR"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/carts/{id}/lines/{lineId}/price-override", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1,
                                  "unitPrice": 1.00,
                                  "reasonCode": "PRICE_MATCH"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "checkout-auth-forbidden-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": 1,
                                  "cashierUserId": 1,
                                  "terminalDeviceId": 1,
                                  "payments": [
                                    {
                                      "tenderType": "CASH",
                                      "amount": 1.00
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    private long createCart() throws Exception {
        return createCart(terminalDeviceId);
    }

    private long createCart(Long terminalId) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/sales/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "storeLocationId": %d,
                                  "terminalDeviceId": %d,
                                  "pricingAt": "2026-02-03T10:00:00Z"
                                }
                                """.formatted(cashierUserId, storeLocationId, terminalId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.lines.length()").value(0))
                .andReturn();

        JsonNode createJson = objectMapper.readTree(createResult.getResponse().getContentAsString());
        return createJson.get("id").asLong();
    }
}
