package com.saulpos.server.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "catalog-admin", authorities = {"PERM_CONFIGURATION_MANAGE"})
class CatalogIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long merchantId;
    private Long categoryId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM open_price_entry_audit");
        jdbcTemplate.execute("DELETE FROM cash_movement");
        jdbcTemplate.execute("DELETE FROM cash_shift");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM category");
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
        merchant.setCode("MER-C1");
        merchant.setName("Catalog Merchant");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);
        merchantId = merchant.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("BEVERAGES");
        category.setName("Beverages");
        category.setActive(true);
        category = categoryRepository.save(category);
        categoryId = category.getId();
    }

    @Test
    void weightedProductModeIsPersistedAndReturned() throws Exception {
        mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "categoryId": %d,
                                  "sku": "sku-weight-001",
                                  "name": "Bulk Rice",
                                  "saleMode": "WEIGHT",
                                  "quantityUom": "KILOGRAM",
                                  "quantityPrecision": 3,
                                  "description": "Weighted item",
                                  "variants": [
                                    {
                                      "code": "unit",
                                      "name": "Unit",
                                      "barcodes": ["7790011111111"]
                                    }
                                  ]
                                }
                                """.formatted(merchantId, categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleMode").value("WEIGHT"))
                .andExpect(jsonPath("$.quantityUom").value("KILOGRAM"))
                .andExpect(jsonPath("$.quantityPrecision").value(3))
                .andExpect(jsonPath("$.openPriceMin").isEmpty())
                .andExpect(jsonPath("$.openPriceMax").isEmpty())
                .andExpect(jsonPath("$.openPriceRequiresReason").value(false));

        mockMvc.perform(get("/api/catalog/products/lookup")
                        .param("merchantId", merchantId.toString())
                        .param("barcode", "7790011111111"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleMode").value("WEIGHT"))
                .andExpect(jsonPath("$.quantityUom").value("KILOGRAM"))
                .andExpect(jsonPath("$.quantityPrecision").value(3));
    }

    @Test
    @WithMockUser(username = "open-price-cashier", authorities = {"PERM_CONFIGURATION_MANAGE", "PERM_OPEN_PRICE_ENTRY"})
    void openPriceValidationRequiresReasonAndAuditsAcceptedEntries() throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "categoryId": %d,
                                  "sku": "sku-open-price-001",
                                  "name": "Custom Service",
                                  "saleMode": "OPEN_PRICE",
                                  "quantityUom": "UNIT",
                                  "quantityPrecision": 0,
                                  "openPriceMin": 5.00,
                                  "openPriceMax": 15.00,
                                  "openPriceRequiresReason": true,
                                  "description": "Open price item",
                                  "variants": [
                                    {
                                      "code": "unit",
                                      "name": "Unit",
                                      "barcodes": ["7790011111122"]
                                    }
                                  ]
                                }
                                """.formatted(merchantId, categoryId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saleMode").value("OPEN_PRICE"))
                .andExpect(jsonPath("$.openPriceMin").value(5.00))
                .andExpect(jsonPath("$.openPriceMax").value(15.00))
                .andExpect(jsonPath("$.openPriceRequiresReason").value(true))
                .andReturn();

        long productId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/catalog/products/{id}/open-price/validate", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "enteredPrice": 10.00
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(post("/api/catalog/products/{id}/open-price/validate", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-Correlation-ID", "corr-open-price-1")
                        .content("""
                                {
                                  "enteredPrice": 9.99,
                                  "reason": "Manual override approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.enteredPrice").value(9.99))
                .andExpect(jsonPath("$.minAllowedPrice").value(5.00))
                .andExpect(jsonPath("$.maxAllowedPrice").value(15.00))
                .andExpect(jsonPath("$.reasonRequired").value(true));

        Integer auditRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM open_price_entry_audit WHERE product_id = ?",
                Integer.class,
                productId);
        org.assertj.core.api.Assertions.assertThat(auditRows).isEqualTo(1);
    }

    @Test
    void productCrudAndLookupFlowWorks() throws Exception {
        Long productId = createProduct("sku-c1-001", "Sparkling Water", "7701234567890", "7701234567891");

        mockMvc.perform(get("/api/catalog/products")
                        .param("merchantId", merchantId.toString())
                        .param("q", "spark"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].sku").value("SKU-C1-001"))
                .andExpect(jsonPath("$[0].variants.length()").value(1));

        mockMvc.perform(get("/api/catalog/products/lookup")
                        .param("merchantId", merchantId.toString())
                        .param("barcode", "7701234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productId").value(productId))
                .andExpect(jsonPath("$.sku").value("SKU-C1-001"))
                .andExpect(jsonPath("$.variantCode").value("UNIT"));

        mockMvc.perform(put("/api/catalog/products/{id}", productId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "categoryId": %d,
                                  "sku": "sku-c1-001",
                                  "name": "Sparkling Water Updated",
                                  "description": "Updated description",
                                  "variants": [
                                    {
                                      "code": "unit",
                                      "name": "Unit",
                                      "barcodes": ["7701234567890", "7701234567892"]
                                    },
                                    {
                                      "code": "pack6",
                                      "name": "Pack of 6",
                                      "barcodes": ["7701234567800"]
                                    }
                                  ]
                                }
                                """.formatted(merchantId, categoryId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Sparkling Water Updated"))
                .andExpect(jsonPath("$.variants.length()").value(2));

        mockMvc.perform(get("/api/catalog/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.variants.length()").value(2));

        mockMvc.perform(post("/api/catalog/products/{id}/deactivate", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/catalog/products/lookup")
                        .param("merchantId", merchantId.toString())
                        .param("barcode", "7701234567890"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));

        mockMvc.perform(post("/api/catalog/products/{id}/activate", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(get("/api/catalog/products/lookup")
                        .param("merchantId", merchantId.toString())
                        .param("barcode", "7701234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName").value("Sparkling Water Updated"));
    }

    @Test
    void duplicateSkuForSameMerchantReturnsConflict() throws Exception {
        createProduct("SKU-DUP-001", "First Product", "7790001112223");

        mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "categoryId": %d,
                                  "sku": "sku-dup-001",
                                  "name": "Second Product",
                                  "variants": [
                                    {
                                      "code": "unit",
                                      "name": "Unit",
                                      "barcodes": ["7790001112224"]
                                    }
                                  ]
                                }
                                """.formatted(merchantId, categoryId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));
    }

    @Test
    void inactiveCategoryCannotReceiveNewProductAssignments() throws Exception {
        CategoryEntity inactiveCategory = new CategoryEntity();
        inactiveCategory.setMerchant(merchantRepository.getReferenceById(merchantId));
        inactiveCategory.setCode("INACTIVE-C2");
        inactiveCategory.setName("Inactive Category");
        inactiveCategory.setActive(false);
        inactiveCategory = categoryRepository.save(inactiveCategory);

        mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "categoryId": %d,
                                  "sku": "sku-inactive-001",
                                  "name": "Blocked Product",
                                  "variants": [
                                    {
                                      "code": "unit",
                                      "name": "Unit",
                                      "barcodes": ["7790001112299"]
                                    }
                                  ]
                                }
                                """.formatted(merchantId, inactiveCategory.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    @Test
    void searchEndpointSupportsPaginationOrderingAndBarcodeMatching() throws Exception {
        createProduct("sku-search-003", "Search Match Three", "7702000000030");
        createProduct("sku-search-001", "Search Match One", "7702000000010");
        createProduct("sku-search-004", "Search Match Four", "7702000000040");
        createProduct("sku-search-002", "Search Match Two", "7702000000020");
        createProduct("sku-ignored-001", "Other Product", "7702999999999");

        mockMvc.perform(get("/api/catalog/products/search")
                        .param("merchantId", merchantId.toString())
                        .param("q", "search")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].sku").value("SKU-SEARCH-001"))
                .andExpect(jsonPath("$.items[1].sku").value("SKU-SEARCH-002"));

        mockMvc.perform(get("/api/catalog/products/search")
                        .param("merchantId", merchantId.toString())
                        .param("q", "search")
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(true))
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].sku").value("SKU-SEARCH-003"))
                .andExpect(jsonPath("$.items[1].sku").value("SKU-SEARCH-004"));

        mockMvc.perform(get("/api/catalog/products/search")
                        .param("merchantId", merchantId.toString())
                        .param("q", "7702000000040")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].sku").value("SKU-SEARCH-004"));
    }

    private Long createProduct(String sku, String name, String... barcodes) throws Exception {
        String barcodeList = java.util.Arrays.stream(barcodes)
                .map(value -> '"' + value + '"')
                .collect(java.util.stream.Collectors.joining(", "));

        MvcResult result = mockMvc.perform(post("/api/catalog/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "categoryId": %d,
                                  "sku": "%s",
                                  "name": "%s",
                                  "description": "Primary item",
                                  "variants": [
                                    {
                                      "code": "unit",
                                      "name": "Unit",
                                      "barcodes": [%s]
                                    }
                                  ]
                                }
                                """.formatted(merchantId, categoryId, sku, name, barcodeList)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(sku.toUpperCase()))
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }
}
