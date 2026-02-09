package com.saulpos.server.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.api.tax.TaxMode;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "inventory-manager", authorities = {"PERM_INVENTORY_ADJUST", "PERM_CONFIGURATION_MANAGE"})
class StockAdjustmentIntegrationTest {

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
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TaxGroupRepository taxGroupRepository;

    @Autowired
    private StoreTaxRuleRepository storeTaxRuleRepository;

    private Long storeLocationId;
    private Long productId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM stock_adjustment");
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

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-H2");
        merchant.setName("Merchant H2");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-H2");
        storeLocation.setName("Store H2");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-H2");
        category.setName("Category H2");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10-H2");
        taxGroup.setName("VAT 10 H2");
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
        product.setSku("SKU-H2-001");
        product.setName("Adjustment Product");
        product.setBasePrice(new BigDecimal("3.00"));
        product.setDescription("Inventory adjustment product");
        product.setActive(true);
        product = productRepository.save(product);
        productId = product.getId();
    }

    @Test
    void largeAdjustmentRequiresApprovalBeforePostAndCreatesMovementOnPost() throws Exception {
        MvcResult createResult = createAdjustment("30.000", "DAMAGED_COUNT", "Damaged package count");
        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long adjustmentId = created.get("id").asLong();
        String referenceNumber = created.get("referenceNumber").asText();

        mockMvc.perform(post("/api/inventory/adjustments/{id}/post", adjustmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/inventory/adjustments/{id}/approve", adjustmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "manager-approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("inventory-manager"));

        mockMvc.perform(post("/api/inventory/adjustments/{id}/post", adjustmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "posted to ledger"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andExpect(jsonPath("$.postedBy").value("inventory-manager"))
                .andExpect(jsonPath("$.inventoryMovementId").isNumber());

        mockMvc.perform(get("/api/inventory/movements")
                        .param("storeLocationId", String.valueOf(storeLocationId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].movementType").value("ADJUSTMENT"))
                .andExpect(jsonPath("$[0].referenceType").value("STOCK_ADJUSTMENT"))
                .andExpect(jsonPath("$[0].referenceNumber").value(referenceNumber))
                .andExpect(jsonPath("$[0].quantityDelta").value(30.000))
                .andExpect(jsonPath("$[0].runningBalance").value(30.000));
    }

    @Test
    void smallAdjustmentAutoApprovesAndCanBePostedDirectly() throws Exception {
        MvcResult createResult = createAdjustment("-2.000", "COUNT_FIX", null);
        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long adjustmentId = created.get("id").asLong();

        mockMvc.perform(post("/api/inventory/adjustments/{id}/post", adjustmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andExpect(jsonPath("$.approvalRequired").value(false))
                .andExpect(jsonPath("$.inventoryMovementId").isNumber());

        mockMvc.perform(get("/api/inventory/balances")
                        .param("storeLocationId", String.valueOf(storeLocationId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantityOnHand").value(-2.000));
    }

    @Test
    @WithMockUser(username = "inventory-only", authorities = {"PERM_INVENTORY_ADJUST"})
    void largeAdjustmentApprovalEndpointRequiresManagerPermission() throws Exception {
        MvcResult createResult = createAdjustment("35.000", "COUNT_VARIANCE", null);
        JsonNode created = objectMapper.readTree(createResult.getResponse().getContentAsString());
        long adjustmentId = created.get("id").asLong();

        mockMvc.perform(post("/api/inventory/adjustments/{id}/approve", adjustmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    private MvcResult createAdjustment(String quantityDelta, String reasonCode, String note) throws Exception {
        String normalizedNote = note == null ? "null" : "\"%s\"".formatted(note);
        return mockMvc.perform(post("/api/inventory/adjustments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "productId": %d,
                                  "quantityDelta": %s,
                                  "reasonCode": "%s",
                                  "note": %s
                                }
                                """.formatted(storeLocationId, productId, quantityDelta, reasonCode, normalizedNote)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reasonCode").value(reasonCode))
                .andExpect(jsonPath("$.referenceNumber").isString())
                .andReturn();
    }
}
