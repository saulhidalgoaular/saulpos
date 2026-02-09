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
@WithMockUser(username = "inventory-counter", authorities = {"PERM_INVENTORY_ADJUST"})
class StocktakeIntegrationTest {

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
    private Long beveragesProductId;
    private Long snacksProductId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM stocktake_line");
        jdbcTemplate.execute("DELETE FROM stocktake_session");
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
        merchant.setCode("MER-H3");
        merchant.setName("Merchant H3");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-H3");
        storeLocation.setName("Store H3");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        CategoryEntity beveragesCategory = new CategoryEntity();
        beveragesCategory.setMerchant(merchant);
        beveragesCategory.setCode("CAT-H3-BEV");
        beveragesCategory.setName("Beverages");
        beveragesCategory.setActive(true);
        beveragesCategory = categoryRepository.save(beveragesCategory);

        CategoryEntity snacksCategory = new CategoryEntity();
        snacksCategory.setMerchant(merchant);
        snacksCategory.setCode("CAT-H3-SNK");
        snacksCategory.setName("Snacks");
        snacksCategory.setActive(true);
        snacksCategory = categoryRepository.save(snacksCategory);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10-H3");
        taxGroup.setName("VAT 10 H3");
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

        ProductEntity beveragesProduct = new ProductEntity();
        beveragesProduct.setMerchant(merchant);
        beveragesProduct.setCategory(beveragesCategory);
        beveragesProduct.setTaxGroup(taxGroup);
        beveragesProduct.setSku("SKU-H3-BEV-001");
        beveragesProduct.setName("Water Bottle");
        beveragesProduct.setBasePrice(new BigDecimal("2.50"));
        beveragesProduct.setDescription("Stocktake product 1");
        beveragesProduct.setActive(true);
        beveragesProduct = productRepository.save(beveragesProduct);
        beveragesProductId = beveragesProduct.getId();

        ProductEntity snacksProduct = new ProductEntity();
        snacksProduct.setMerchant(merchant);
        snacksProduct.setCategory(snacksCategory);
        snacksProduct.setTaxGroup(taxGroup);
        snacksProduct.setSku("SKU-H3-SNK-001");
        snacksProduct.setName("Potato Chips");
        snacksProduct.setBasePrice(new BigDecimal("3.20"));
        snacksProduct.setDescription("Stocktake product 2");
        snacksProduct.setActive(true);
        snacksProduct = productRepository.save(snacksProduct);
        snacksProductId = snacksProduct.getId();
    }

    @Test
    void stocktakeFinalizePostsVarianceAndExposesVarianceReportByProductAndCategory() throws Exception {
        createManualAdjustmentMovement(beveragesProductId, "10.000", "ADJ-H3-BEV-OPEN");
        createManualAdjustmentMovement(snacksProductId, "5.000", "ADJ-H3-SNK-OPEN");

        JsonNode created = createStocktake();
        long stocktakeId = created.get("id").asLong();

        mockMvc.perform(post("/api/inventory/stocktakes/{id}/start", stocktakeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STARTED"))
                .andExpect(jsonPath("$.snapshotAt").isNotEmpty())
                .andExpect(jsonPath("$.lines.length()").value(2))
                .andExpect(jsonPath("$.lines[0].expectedQuantity").value(10.000))
                .andExpect(jsonPath("$.lines[1].expectedQuantity").value(5.000));

        mockMvc.perform(post("/api/inventory/stocktakes/{id}/finalize", stocktakeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "countedQuantity": 7.000
                                    },
                                    {
                                      "productId": %d,
                                      "countedQuantity": 8.000
                                    }
                                  ]
                                }
                                """.formatted(beveragesProductId, snacksProductId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZED"))
                .andExpect(jsonPath("$.finalizedBy").value("inventory-counter"))
                .andExpect(jsonPath("$.lines[0].varianceQuantity").value(-3.000))
                .andExpect(jsonPath("$.lines[0].inventoryMovementId").isNumber())
                .andExpect(jsonPath("$.lines[1].varianceQuantity").value(3.000))
                .andExpect(jsonPath("$.lines[1].inventoryMovementId").isNumber());

        mockMvc.perform(get("/api/inventory/movements")
                        .param("storeLocationId", String.valueOf(storeLocationId))
                        .param("productId", String.valueOf(beveragesProductId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].movementType").value("ADJUSTMENT"))
                .andExpect(jsonPath("$[1].referenceType").value("STOCKTAKE"))
                .andExpect(jsonPath("$[1].quantityDelta").value(-3.000));

        mockMvc.perform(get("/api/inventory/stocktakes/{id}/variance", stocktakeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZED"))
                .andExpect(jsonPath("$.byProduct.length()").value(2))
                .andExpect(jsonPath("$.byProduct[0].categoryName").value("Beverages"))
                .andExpect(jsonPath("$.byProduct[0].varianceQuantity").value(-3.000))
                .andExpect(jsonPath("$.byProduct[1].categoryName").value("Snacks"))
                .andExpect(jsonPath("$.byProduct[1].varianceQuantity").value(3.000))
                .andExpect(jsonPath("$.byCategory.length()").value(2))
                .andExpect(jsonPath("$.byCategory[0].categoryName").value("Beverages"))
                .andExpect(jsonPath("$.byCategory[0].varianceQuantity").value(-3.000))
                .andExpect(jsonPath("$.byCategory[1].categoryName").value("Snacks"))
                .andExpect(jsonPath("$.byCategory[1].varianceQuantity").value(3.000));
    }

    @Test
    void stocktakeFinalizeRejectsMissingCountLines() throws Exception {
        createManualAdjustmentMovement(beveragesProductId, "4.000", "ADJ-H3-BEV-SINGLE");

        JsonNode created = createStocktake();
        long stocktakeId = created.get("id").asLong();

        mockMvc.perform(post("/api/inventory/stocktakes/{id}/start", stocktakeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("STARTED"));

        mockMvc.perform(post("/api/inventory/stocktakes/{id}/finalize", stocktakeId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "countedQuantity": 4.000
                                    }
                                  ]
                                }
                                """.formatted(beveragesProductId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    private JsonNode createStocktake() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/inventory/stocktakes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "productIds": [%d, %d],
                                  "note": "cycle-count-h3"
                                }
                                """.formatted(storeLocationId, beveragesProductId, snacksProductId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.lines.length()").value(2))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void createManualAdjustmentMovement(Long productId,
                                                String quantityDelta,
                                                String referenceNumber) throws Exception {
        mockMvc.perform(post("/api/inventory/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "productId": %d,
                                  "movementType": "ADJUSTMENT",
                                  "quantityDelta": %s,
                                  "referenceType": "STOCK_ADJUSTMENT",
                                  "referenceNumber": "%s"
                                }
                                """.formatted(storeLocationId, productId, quantityDelta, referenceNumber)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementType").value("ADJUSTMENT"));
    }
}
