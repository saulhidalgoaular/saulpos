package com.saulpos.server.uat;

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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "p2-cashier", authorities = {"PERM_SALES_PROCESS", "PERM_CONFIGURATION_MANAGE"})
class P2PerformanceReliabilityIntegrationTest {

    private static final long CHECKOUT_P95_TARGET_MILLIS = 1800L;
    private static final long LOOKUP_P95_TARGET_MILLIS = 1000L;

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
    private Long merchantId;
    private Long storeLocationId;
    private Long terminalDeviceId;
    private Long productId;

    @BeforeEach
    void setUp() {
        List<String> tables = List.of(
                "idempotency_key_event",
                "payment_transition",
                "payment_allocation",
                "payment",
                "inventory_movement",
                "sale_line",
                "sale",
                "sale_override_event",
                "void_reason_code",
                "sale_cart_event",
                "parked_cart_reference",
                "sale_cart_line",
                "sale_cart",
                "receipt_header",
                "receipt_sequence",
                "receipt_series",
                "rounding_policy",
                "store_tax_rule",
                "tax_group",
                "open_price_entry_audit",
                "store_price_override",
                "price_book_item",
                "price_book",
                "product_barcode",
                "product_variant",
                "product",
                "category",
                "cash_movement",
                "cash_shift",
                "store_user_assignment",
                "terminal_device",
                "store_location",
                "merchant",
                "auth_audit_event",
                "auth_session",
                "user_role",
                "role_permission",
                "app_permission",
                "app_role",
                "user_account");

        for (String table : tables) {
            jdbcTemplate.execute("DELETE FROM " + table);
        }

        UserAccountEntity cashier = new UserAccountEntity();
        cashier.setUsername("p2-cashier-user");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashierUserId = userAccountRepository.save(cashier).getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-P2");
        merchant.setName("Merchant P2");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);
        merchantId = merchant.getId();

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode("STORE-P2");
        store.setName("Store P2");
        store.setActive(true);
        store = storeLocationRepository.save(store);
        storeLocationId = store.getId();

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode("TERM-P2");
        terminal.setName("Terminal P2");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);
        terminalDeviceId = terminal.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-P2");
        category.setName("Category P2");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10-P2");
        taxGroup.setName("VAT 10 P2");
        taxGroup.setTaxRatePercent(new BigDecimal("10.0000"));
        taxGroup.setZeroRated(false);
        taxGroup.setActive(true);
        taxGroup = taxGroupRepository.save(taxGroup);

        StoreTaxRuleEntity taxRule = new StoreTaxRuleEntity();
        taxRule.setStoreLocation(store);
        taxRule.setTaxGroup(taxGroup);
        taxRule.setTaxMode(TaxMode.EXCLUSIVE);
        taxRule.setExempt(false);
        taxRule.setActive(true);
        storeTaxRuleRepository.save(taxRule);

        ProductEntity product = new ProductEntity();
        product.setMerchant(merchant);
        product.setCategory(category);
        product.setTaxGroup(taxGroup);
        product.setSku("SKU-P2-001");
        product.setName("P2 Load Product");
        product.setBasePrice(new BigDecimal("5.00"));
        product.setDescription("Performance reliability product");
        product.setActive(true);
        product = productRepository.save(product);
        productId = product.getId();
    }

    @Test
    void lookupAndCheckoutMeetDefinedP95TargetsForBurstTraffic() throws Exception {
        int warmupIterations = 3;
        int measuredIterations = 20;
        List<Long> lookupLatenciesMillis = new ArrayList<>();
        List<Long> checkoutLatenciesMillis = new ArrayList<>();

        for (int i = 0; i < warmupIterations + measuredIterations; i++) {
            long lookupStartedNanos = System.nanoTime();
            mockMvc.perform(get("/api/catalog/products/search")
                            .param("merchantId", String.valueOf(merchantId))
                            .param("q", "p2 load")
                            .param("active", "true")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk());
            long lookupLatencyMillis = nanosToMillis(System.nanoTime() - lookupStartedNanos);

            long cartId = createCart(i);
            addLine(cartId);

            long checkoutStartedNanos = System.nanoTime();
            mockMvc.perform(post("/api/sales/checkout")
                            .header("Idempotency-Key", "p2-load-checkout-" + i)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                      "cartId": %d,
                                      "cashierUserId": %d,
                                      "terminalDeviceId": %d,
                                      "payments": [
                                        {
                                          "tenderType": "CASH",
                                          "amount": 5.50,
                                          "tenderedAmount": 5.50
                                        }
                                      ]
                                    }
                                    """.formatted(cartId, cashierUserId, terminalDeviceId)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.saleId").isNumber());
            long checkoutLatencyMillis = nanosToMillis(System.nanoTime() - checkoutStartedNanos);

            if (i >= warmupIterations) {
                lookupLatenciesMillis.add(lookupLatencyMillis);
                checkoutLatenciesMillis.add(checkoutLatencyMillis);
            }
        }

        long lookupP95Millis = p95Millis(lookupLatenciesMillis);
        long checkoutP95Millis = p95Millis(checkoutLatenciesMillis);

        assertThat(lookupP95Millis).isLessThanOrEqualTo(LOOKUP_P95_TARGET_MILLIS);
        assertThat(checkoutP95Millis).isLessThanOrEqualTo(CHECKOUT_P95_TARGET_MILLIS);
    }

    @Test
    void checkoutReplayWithSameIdempotencyKeyReturnsSameResultAndNoDuplicateWrites() throws Exception {
        long cartId = createCart(99);
        addLine(cartId);
        String idempotencyKey = "p2-replay-same-key-1";

        String payload = """
                {
                  "cartId": %d,
                  "cashierUserId": %d,
                  "terminalDeviceId": %d,
                  "payments": [
                    {
                      "tenderType": "CASH",
                      "amount": 5.50,
                      "tenderedAmount": 5.50
                    }
                  ]
                }
                """.formatted(cartId, cashierUserId, terminalDeviceId);

        MvcResult first = mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        MvcResult replay = mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", idempotencyKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode firstJson = objectMapper.readTree(first.getResponse().getContentAsString());
        JsonNode replayJson = objectMapper.readTree(replay.getResponse().getContentAsString());
        assertThat(replayJson.path("saleId").asLong()).isEqualTo(firstJson.path("saleId").asLong());
        assertThat(replayJson.path("receiptNumber").asText()).isEqualTo(firstJson.path("receiptNumber").asText());

        Integer saleRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sale", Integer.class);
        Integer paymentRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment", Integer.class);
        Integer movementRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventory_movement", Integer.class);
        assertThat(saleRows).isEqualTo(1);
        assertThat(paymentRows).isEqualTo(1);
        assertThat(movementRows).isEqualTo(1);
    }

    @Test
    void transientCheckoutValidationFailureCanRecoverWithoutDataCorruption() throws Exception {
        long cartId = createCart(101);
        addLine(cartId);

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "p2-transient-failure-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CASH",
                                      "amount": 1.00,
                                      "tenderedAmount": 1.00
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        Integer failedSaleRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sale", Integer.class);
        Integer failedPaymentRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment", Integer.class);
        assertThat(failedSaleRows).isEqualTo(0);
        assertThat(failedPaymentRows).isEqualTo(0);

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "p2-transient-failure-2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CASH",
                                      "amount": 5.50,
                                      "tenderedAmount": 5.50
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").isNumber());

        Integer recoveredSaleRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM sale", Integer.class);
        Integer recoveredPaymentRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM payment", Integer.class);
        Integer recoveredMovementRows = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM inventory_movement", Integer.class);
        assertThat(recoveredSaleRows).isEqualTo(1);
        assertThat(recoveredPaymentRows).isEqualTo(1);
        assertThat(recoveredMovementRows).isEqualTo(1);
    }

    private long createCart(int seed) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sales/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "storeLocationId": %d,
                                  "terminalDeviceId": %d,
                                  "pricingAt": "2026-02-10T10:%02d:00Z"
                                }
                                """.formatted(cashierUserId, storeLocationId, terminalDeviceId, seed % 60)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private void addLine(long cartId) throws Exception {
        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "p2-line-%d",
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(cartId, productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayable").value(5.50));
    }

    private long readId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("id").asLong();
    }

    private static long nanosToMillis(long nanos) {
        return nanos / 1_000_000L;
    }

    private static long p95Millis(List<Long> values) {
        assertThat(values).isNotEmpty();
        List<Long> sorted = values.stream().sorted(Comparator.naturalOrder()).toList();
        int index = (int) Math.ceil(sorted.size() * 0.95d) - 1;
        return sorted.get(Math.max(index, 0));
    }
}
