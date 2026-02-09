package com.saulpos.server.discount;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.api.tax.TaxMode;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.discount.model.DiscountReasonCodeEntity;
import com.saulpos.server.discount.repository.DiscountReasonCodeRepository;
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
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS", "PERM_DISCOUNT_OVERRIDE"})
class DiscountIntegrationTest {

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
    private DiscountReasonCodeRepository discountReasonCodeRepository;

    @Autowired
    private TaxGroupRepository taxGroupRepository;

    @Autowired
    private StoreTaxRuleRepository storeTaxRuleRepository;

    private MerchantEntity merchant;
    private StoreLocationEntity storeLocation;
    private ProductEntity productA;
    private ProductEntity productB;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
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
        jdbcTemplate.execute("DELETE FROM inventory_movement");
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
        merchant.setCode("MER-E1");
        merchant.setName("Merchant E1");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-E1");
        storeLocation.setName("Store E1");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-E1");
        category.setName("Category E1");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT18");
        taxGroup.setName("VAT 18");
        taxGroup.setTaxRatePercent(new BigDecimal("18.0000"));
        taxGroup.setZeroRated(false);
        taxGroup.setActive(true);
        taxGroup = taxGroupRepository.save(taxGroup);

        StoreTaxRuleEntity taxRule = new StoreTaxRuleEntity();
        taxRule.setStoreLocation(storeLocation);
        taxRule.setTaxGroup(taxGroup);
        taxRule.setTaxMode(TaxMode.EXCLUSIVE);
        taxRule.setExempt(false);
        taxRule.setActive(true);
        taxRule.setEffectiveFrom(Instant.parse("2020-01-01T00:00:00Z"));
        storeTaxRuleRepository.save(taxRule);

        productA = new ProductEntity();
        productA.setMerchant(merchant);
        productA.setCategory(category);
        productA.setTaxGroup(taxGroup);
        productA.setSku("SKU-E1-A");
        productA.setName("Discount Product A");
        productA.setBasePrice(new BigDecimal("10.00"));
        productA.setActive(true);
        productA = productRepository.save(productA);

        productB = new ProductEntity();
        productB.setMerchant(merchant);
        productB.setCategory(category);
        productB.setTaxGroup(taxGroup);
        productB.setSku("SKU-E1-B");
        productB.setName("Discount Product B");
        productB.setBasePrice(new BigDecimal("20.00"));
        productB.setActive(true);
        productB = productRepository.save(productB);

        DiscountReasonCodeEntity reasonCode = new DiscountReasonCodeEntity();
        reasonCode.setMerchant(merchant);
        reasonCode.setCode("PRICE_MATCH");
        reasonCode.setDescription("Price match");
        reasonCode.setActive(true);
        discountReasonCodeRepository.save(reasonCode);
    }

    @Test
    void applyAndPreviewReflectDiscountsInTaxAndTotals() throws Exception {
        mockMvc.perform(post("/api/discounts/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "contextKey": "ctx-e1-1",
                                  "scope": "LINE",
                                  "productId": %d,
                                  "type": "PERCENTAGE",
                                  "value": 10.0,
                                  "reasonCode": "price_match",
                                  "note": "manual line discount"
                                }
                                """.formatted(storeLocation.getId(), productA.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scope").value("LINE"))
                .andExpect(jsonPath("$.managerApprovalRequired").value(false));

        mockMvc.perform(post("/api/discounts/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "contextKey": "ctx-e1-1",
                                  "scope": "CART",
                                  "type": "FIXED",
                                  "value": 5.0,
                                  "reasonCode": "PRICE_MATCH"
                                }
                                """.formatted(storeLocation.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.scope").value("CART"));

        mockMvc.perform(post("/api/discounts/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "contextKey": "ctx-e1-1",
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "quantity": 1.000
                                    },
                                    {
                                      "productId": %d,
                                      "quantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(storeLocation.getId(), productA.getId(), productB.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotalBeforeDiscount").value(30.00))
                .andExpect(jsonPath("$.totalDiscount").value(6.00))
                .andExpect(jsonPath("$.subtotalAfterDiscount").value(24.00))
                .andExpect(jsonPath("$.appliedDiscounts.length()").value(2))
                .andExpect(jsonPath("$.lines[0].discountedUnitPrice").value(7.45))
                .andExpect(jsonPath("$.lines[1].discountedUnitPrice").value(16.55))
                .andExpect(jsonPath("$.taxPreview.totalTax").value(4.32))
                .andExpect(jsonPath("$.taxPreview.totalGross").value(28.32));
    }

    @Test
    @WithMockUser(username = "cashier-no-override", authorities = {"PERM_SALES_PROCESS"})
    void highThresholdDiscountRequiresOverridePermission() throws Exception {
        mockMvc.perform(post("/api/discounts/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "contextKey": "ctx-e1-2",
                                  "scope": "LINE",
                                  "productId": %d,
                                  "type": "PERCENTAGE",
                                  "value": 20.0,
                                  "reasonCode": "PRICE_MATCH"
                                }
                                """.formatted(storeLocation.getId(), productA.getId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    @Test
    void removeDeactivatesDiscountAndPreviewStopsApplyingIt() throws Exception {
        MvcResult applyResult = mockMvc.perform(post("/api/discounts/apply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "contextKey": "ctx-e1-3",
                                  "scope": "LINE",
                                  "productId": %d,
                                  "type": "FIXED",
                                  "value": 2.0,
                                  "reasonCode": "PRICE_MATCH"
                                }
                                """.formatted(storeLocation.getId(), productA.getId())))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode applyJson = objectMapper.readTree(applyResult.getResponse().getContentAsString());
        long discountId = applyJson.get("id").asLong();

        mockMvc.perform(post("/api/discounts/{id}/remove", discountId)
                        .param("storeLocationId", storeLocation.getId().toString())
                        .param("contextKey", "ctx-e1-3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.removed").value(true))
                .andExpect(jsonPath("$.removedByUsername").value("cashier"));

        mockMvc.perform(post("/api/discounts/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "contextKey": "ctx-e1-3",
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "quantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(storeLocation.getId(), productA.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.appliedDiscounts.length()").value(0))
                .andExpect(jsonPath("$.totalDiscount").value(0.00));

        Integer activeRows = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM discount_application WHERE id = ? AND is_active = TRUE",
                Integer.class,
                discountId);
        assertThat(activeRows).isEqualTo(0);
    }
}
