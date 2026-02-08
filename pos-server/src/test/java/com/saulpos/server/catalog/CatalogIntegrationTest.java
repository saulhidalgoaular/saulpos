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
