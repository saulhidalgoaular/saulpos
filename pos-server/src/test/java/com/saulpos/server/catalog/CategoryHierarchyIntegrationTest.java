package com.saulpos.server.catalog;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "catalog-admin", authorities = {"PERM_CONFIGURATION_MANAGE"})
class CategoryHierarchyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Long merchantId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
        jdbcTemplate.execute("DELETE FROM cash_movement");
        jdbcTemplate.execute("DELETE FROM cash_shift");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM inventory_movement");
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
        merchant.setCode("MER-C2");
        merchant.setName("Merchant C2");
        merchant.setActive(true);
        merchantId = merchantRepository.save(merchant).getId();
    }

    @Test
    void treeEndpointReturnsNestedHierarchy() throws Exception {
        CategoryEntity beverages = createCategory("BEVERAGES", "Beverages", null);
        createCategory("SODA", "Soda", beverages);
        createCategory("WATER", "Water", beverages);
        createCategory("SNACKS", "Snacks", null);

        mockMvc.perform(get("/api/catalog/categories/tree")
                        .param("merchantId", merchantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("BEVERAGES"))
                .andExpect(jsonPath("$[0].children.length()").value(2))
                .andExpect(jsonPath("$[1].code").value("SNACKS"));
    }

    @Test
    void reparentRejectsCycles() throws Exception {
        CategoryEntity root = createCategory("ROOT", "Root", null);
        CategoryEntity middle = createCategory("MIDDLE", "Middle", root);
        CategoryEntity leaf = createCategory("LEAF", "Leaf", middle);

        mockMvc.perform(post("/api/catalog/categories/{id}/reparent", root.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "parentId": %d
                                }
                                """.formatted(merchantId, leaf.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    @Test
    void reparentMovesCategoryToNewParent() throws Exception {
        CategoryEntity beverages = createCategory("BEVERAGES", "Beverages", null);
        CategoryEntity snacks = createCategory("SNACKS", "Snacks", null);
        CategoryEntity juice = createCategory("JUICE", "Juice", beverages);

        mockMvc.perform(post("/api/catalog/categories/{id}/reparent", juice.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "parentId": %d
                                }
                                """.formatted(merchantId, snacks.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(juice.getId()))
                .andExpect(jsonPath("$.parentId").value(snacks.getId()));

        mockMvc.perform(get("/api/catalog/categories/tree")
                        .param("merchantId", merchantId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("BEVERAGES"))
                .andExpect(jsonPath("$[0].children.length()").value(0))
                .andExpect(jsonPath("$[1].code").value("SNACKS"))
                .andExpect(jsonPath("$[1].children[0].code").value("JUICE"));
    }

    private CategoryEntity createCategory(String code, String name, CategoryEntity parent) {
        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchantRepository.getReferenceById(merchantId));
        category.setCode(code);
        category.setName(name);
        category.setActive(true);
        category.setParent(parent);
        return categoryRepository.save(category);
    }
}
