package com.saulpos.server.promotion;

import com.saulpos.api.promotion.PromotionRuleType;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.promotion.model.PromotionEntity;
import com.saulpos.server.promotion.model.PromotionRuleEntity;
import com.saulpos.server.promotion.model.PromotionWindowEntity;
import com.saulpos.server.promotion.repository.PromotionRepository;
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

import java.math.BigDecimal;
import java.time.Instant;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class PromotionIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    private PromotionRepository promotionRepository;

    private MerchantEntity merchant;
    private StoreLocationEntity storeLocation;
    private ProductEntity productA;
    private ProductEntity productB;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM promotion_window");
        jdbcTemplate.execute("DELETE FROM promotion_rule");
        jdbcTemplate.execute("DELETE FROM promotion");
        jdbcTemplate.execute("DELETE FROM discount_application");
        jdbcTemplate.execute("DELETE FROM discount_reason_code");
        jdbcTemplate.execute("DELETE FROM rounding_policy");
        jdbcTemplate.execute("DELETE FROM store_tax_rule");
        jdbcTemplate.execute("DELETE FROM open_price_entry_audit");
        jdbcTemplate.execute("DELETE FROM store_price_override");
        jdbcTemplate.execute("DELETE FROM price_book_item");
        jdbcTemplate.execute("DELETE FROM price_book");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM tax_group");
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

        merchant = new MerchantEntity();
        merchant.setCode("MER-P1");
        merchant.setName("Merchant P1");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-P1");
        storeLocation.setName("Store P1");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-P1");
        category.setName("Category P1");
        category.setActive(true);
        category = categoryRepository.save(category);

        productA = new ProductEntity();
        productA.setMerchant(merchant);
        productA.setCategory(category);
        productA.setSku("SKU-P1-A");
        productA.setName("Promotion Product A");
        productA.setBasePrice(new BigDecimal("10.00"));
        productA.setActive(true);
        productA = productRepository.save(productA);

        productB = new ProductEntity();
        productB.setMerchant(merchant);
        productB.setCategory(category);
        productB.setSku("SKU-P1-B");
        productB.setName("Promotion Product B");
        productB.setBasePrice(new BigDecimal("5.00"));
        productB.setActive(true);
        productB = productRepository.save(productB);
    }

    @Test
    void evaluateReturnsExplanationForAppliedPromotion() throws Exception {
        promotionRepository.save(productPercentagePromotion("PROMO10", 5, productA, "10.0000"));

        mockMvc.perform(post("/api/promotions/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "quantity": 2.000,
                                      "unitPrice": 10.00
                                    },
                                    {
                                      "productId": %d,
                                      "quantity": 1.000,
                                      "unitPrice": 5.00
                                    }
                                  ]
                                }
                                """.formatted(storeLocation.getId(), productA.getId(), productB.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedPromotion.code").value("PROMO10"))
                .andExpect(jsonPath("$.subtotalBeforeDiscount").value(25.00))
                .andExpect(jsonPath("$.totalDiscount").value(2.00))
                .andExpect(jsonPath("$.subtotalAfterDiscount").value(23.00))
                .andExpect(jsonPath("$.lines[0].discountedUnitPrice").value(9.00))
                .andExpect(jsonPath("$.appliedPromotion.explanations[0]", containsString("SKU-P1-A")));
    }

    @Test
    void overlappingPromotionsUseDeterministicPriorityWinner() throws Exception {
        promotionRepository.save(productPercentagePromotion("HIGH_PRIORITY", 20, productA, "10.0000"));
        promotionRepository.save(cartFixedPromotion("LOW_PRIORITY", 10, "5.0000"));

        mockMvc.perform(post("/api/promotions/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "quantity": 2.000,
                                      "unitPrice": 10.00
                                    }
                                  ]
                                }
                                """.formatted(storeLocation.getId(), productA.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedPromotion.code").value("HIGH_PRIORITY"))
                .andExpect(jsonPath("$.appliedPromotion.priority").value(20))
                .andExpect(jsonPath("$.totalDiscount").value(2.00));
    }

    private PromotionEntity productPercentagePromotion(String code,
                                                       int priority,
                                                       ProductEntity targetProduct,
                                                       String percentage) {
        PromotionEntity promotion = basePromotion(code, priority);

        PromotionRuleEntity rule = new PromotionRuleEntity();
        rule.setRuleType(PromotionRuleType.PRODUCT_PERCENTAGE);
        rule.setTargetProduct(targetProduct);
        rule.setDiscountValue(new BigDecimal(percentage));
        rule.setMinQuantity(new BigDecimal("1.000"));
        rule.setActive(true);
        promotion.addRule(rule);

        promotion.addWindow(activeWindow());
        return promotion;
    }

    private PromotionEntity cartFixedPromotion(String code, int priority, String amount) {
        PromotionEntity promotion = basePromotion(code, priority);

        PromotionRuleEntity rule = new PromotionRuleEntity();
        rule.setRuleType(PromotionRuleType.CART_FIXED);
        rule.setDiscountValue(new BigDecimal(amount));
        rule.setMinSubtotal(new BigDecimal("0.00"));
        rule.setActive(true);
        promotion.addRule(rule);

        promotion.addWindow(activeWindow());
        return promotion;
    }

    private PromotionEntity basePromotion(String code, int priority) {
        PromotionEntity promotion = new PromotionEntity();
        promotion.setMerchant(merchant);
        promotion.setCode(code);
        promotion.setName(code + " Name");
        promotion.setDescription(code + " Description");
        promotion.setPriority(priority);
        promotion.setActive(true);
        return promotion;
    }

    private PromotionWindowEntity activeWindow() {
        PromotionWindowEntity window = new PromotionWindowEntity();
        window.setStartsAt(Instant.parse("2026-01-01T00:00:00Z"));
        window.setEndsAt(Instant.parse("2026-12-31T23:59:59Z"));
        window.setActive(true);
        return window;
    }
}
