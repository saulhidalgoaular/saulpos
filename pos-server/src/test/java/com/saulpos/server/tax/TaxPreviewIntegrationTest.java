package com.saulpos.server.tax;

import com.saulpos.api.tax.RoundingMethod;
import com.saulpos.api.tax.TaxMode;
import com.saulpos.api.tax.TenderType;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.tax.model.RoundingPolicyEntity;
import com.saulpos.server.tax.model.StoreTaxRuleEntity;
import com.saulpos.server.tax.model.TaxGroupEntity;
import com.saulpos.server.tax.repository.RoundingPolicyRepository;
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

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class TaxPreviewIntegrationTest {

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
    private TaxGroupRepository taxGroupRepository;

    @Autowired
    private StoreTaxRuleRepository storeTaxRuleRepository;

    @Autowired
    private RoundingPolicyRepository roundingPolicyRepository;

    private MerchantEntity merchant;
    private StoreLocationEntity storeLocation;
    private CategoryEntity category;

    @BeforeEach
    void setUp() {
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
        merchant.setCode("MER-D1");
        merchant.setName("Merchant D1");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-D1");
        storeLocation.setName("Store D1");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);

        category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-D1");
        category.setName("Category D1");
        category.setActive(true);
        category = categoryRepository.save(category);
    }

    @Test
    void previewReturnsExclusiveTaxBreakdownAndTotals() throws Exception {
        TaxGroupEntity vat18 = createTaxGroup("VAT18", "VAT 18", "18.0000", false);
        ProductEntity product = createProduct("SKU-D1-EXCL", "Exclusive Product", "10.00", vat18);
        createStoreTaxRule(vat18, TaxMode.EXCLUSIVE, false);

        mockMvc.perform(post("/api/tax/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "quantity": 2.000
                                    }
                                  ]
                                }
                                """.formatted(storeLocation.getId(), product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotalNet").value(20.00))
                .andExpect(jsonPath("$.totalTax").value(3.60))
                .andExpect(jsonPath("$.totalGross").value(23.60))
                .andExpect(jsonPath("$.lines[0].taxGroupCode").value("VAT18"))
                .andExpect(jsonPath("$.lines[0].taxMode").value("EXCLUSIVE"))
                .andExpect(jsonPath("$.lines[0].taxRatePercent").value(18.0000))
                .andExpect(jsonPath("$.lines[0].netAmount").value(20.00))
                .andExpect(jsonPath("$.lines[0].taxAmount").value(3.60))
                .andExpect(jsonPath("$.lines[0].grossAmount").value(23.60));
    }

    @Test
    void previewHandlesInclusiveExemptAndZeroRatedLinesDeterministically() throws Exception {
        TaxGroupEntity vat18 = createTaxGroup("VAT18", "VAT 18", "18.0000", false);
        TaxGroupEntity exempt18 = createTaxGroup("EXEMPT18", "Exempt Group", "18.0000", false);
        TaxGroupEntity zeroRated = createTaxGroup("ZERO", "Zero Rated", "0.0000", true);

        ProductEntity inclusiveProduct = createProduct("SKU-D1-INCL", "Inclusive Product", "11.80", vat18);
        ProductEntity exemptProduct = createProduct("SKU-D1-EXEMPT", "Exempt Product", "5.00", exempt18);
        ProductEntity zeroRatedProduct = createProduct("SKU-D1-ZERO", "Zero Product", "7.25", zeroRated);

        createStoreTaxRule(vat18, TaxMode.INCLUSIVE, false);
        createStoreTaxRule(exempt18, TaxMode.EXCLUSIVE, true);
        createStoreTaxRule(zeroRated, TaxMode.EXCLUSIVE, false);

        mockMvc.perform(post("/api/tax/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "quantity": 1.000,
                                      "unitPrice": 11.80
                                    },
                                    {
                                      "productId": %d,
                                      "quantity": 2.000,
                                      "unitPrice": 5.00
                                    },
                                    {
                                      "productId": %d,
                                      "quantity": 1.000,
                                      "unitPrice": 7.25
                                    }
                                  ]
                                }
                                """.formatted(storeLocation.getId(),
                                inclusiveProduct.getId(),
                                exemptProduct.getId(),
                                zeroRatedProduct.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotalNet").value(27.25))
                .andExpect(jsonPath("$.totalTax").value(1.80))
                .andExpect(jsonPath("$.totalGross").value(29.05))
                .andExpect(jsonPath("$.lines[0].taxMode").value("INCLUSIVE"))
                .andExpect(jsonPath("$.lines[0].taxAmount").value(1.80))
                .andExpect(jsonPath("$.lines[1].exempt").value(true))
                .andExpect(jsonPath("$.lines[1].taxAmount").value(0.00))
                .andExpect(jsonPath("$.lines[2].zeroRated").value(true))
                .andExpect(jsonPath("$.lines[2].taxAmount").value(0.00));
    }

    @Test
    void previewRejectsProductsWithoutApplicableStoreTaxRule() throws Exception {
        TaxGroupEntity vat18 = createTaxGroup("VAT18", "VAT 18", "18.0000", false);
        ProductEntity product = createProduct("SKU-D1-NORULE", "No Rule Product", "10.00", vat18);

        mockMvc.perform(post("/api/tax/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "at": "2026-02-10T12:00:00Z",
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "quantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(storeLocation.getId(), product.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    @Test
    void previewAppliesCashRoundingPolicyAndReturnsExplicitAdjustmentLine() throws Exception {
        TaxGroupEntity vat18 = createTaxGroup("VAT18", "VAT 18", "18.0000", false);
        ProductEntity product = createProduct("SKU-D2-CASH", "Cash Rounded Product", "8.50", vat18);
        createStoreTaxRule(vat18, TaxMode.EXCLUSIVE, false);
        createRoundingPolicy(TenderType.CASH, RoundingMethod.NEAREST, "0.05");

        mockMvc.perform(post("/api/tax/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "at": "2026-02-10T12:00:00Z",
                                  "tenderType": "CASH",
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "quantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(storeLocation.getId(), product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGross").value(10.03))
                .andExpect(jsonPath("$.roundingAdjustment").value(0.02))
                .andExpect(jsonPath("$.totalPayable").value(10.05))
                .andExpect(jsonPath("$.rounding.applied").value(true))
                .andExpect(jsonPath("$.rounding.tenderType").value("CASH"))
                .andExpect(jsonPath("$.rounding.method").value("NEAREST"))
                .andExpect(jsonPath("$.rounding.increment").value(0.05))
                .andExpect(jsonPath("$.rounding.roundedAmount").value(10.05));
    }

    @Test
    void previewKeepsPayableEqualToGrossWhenTenderRoundingPolicyIsMissing() throws Exception {
        TaxGroupEntity vat18 = createTaxGroup("VAT18", "VAT 18", "18.0000", false);
        ProductEntity product = createProduct("SKU-D2-CARD", "Card Product", "8.50", vat18);
        createStoreTaxRule(vat18, TaxMode.EXCLUSIVE, false);

        mockMvc.perform(post("/api/tax/preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "at": "2026-02-10T12:00:00Z",
                                  "tenderType": "CARD",
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "quantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(storeLocation.getId(), product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGross").value(10.03))
                .andExpect(jsonPath("$.roundingAdjustment").value(0.00))
                .andExpect(jsonPath("$.totalPayable").value(10.03))
                .andExpect(jsonPath("$.rounding.applied").value(false))
                .andExpect(jsonPath("$.rounding.tenderType").value("CARD"))
                .andExpect(jsonPath("$.rounding.roundedAmount").value(10.03));
    }

    private TaxGroupEntity createTaxGroup(String code, String name, String ratePercent, boolean zeroRated) {
        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode(code);
        taxGroup.setName(name);
        taxGroup.setTaxRatePercent(new BigDecimal(ratePercent));
        taxGroup.setZeroRated(zeroRated);
        taxGroup.setActive(true);
        return taxGroupRepository.save(taxGroup);
    }

    private ProductEntity createProduct(String sku, String name, String basePrice, TaxGroupEntity taxGroup) {
        ProductEntity product = new ProductEntity();
        product.setMerchant(merchant);
        product.setCategory(category);
        product.setTaxGroup(taxGroup);
        product.setSku(sku);
        product.setName(name);
        product.setBasePrice(new BigDecimal(basePrice));
        product.setDescription(name);
        product.setActive(true);
        return productRepository.save(product);
    }

    private StoreTaxRuleEntity createStoreTaxRule(TaxGroupEntity taxGroup, TaxMode taxMode, boolean exempt) {
        StoreTaxRuleEntity taxRule = new StoreTaxRuleEntity();
        taxRule.setStoreLocation(storeLocation);
        taxRule.setTaxGroup(taxGroup);
        taxRule.setTaxMode(taxMode);
        taxRule.setExempt(exempt);
        taxRule.setActive(true);
        taxRule.setEffectiveFrom(Instant.parse("2026-01-01T00:00:00Z"));
        return storeTaxRuleRepository.save(taxRule);
    }

    private RoundingPolicyEntity createRoundingPolicy(TenderType tenderType,
                                                      RoundingMethod method,
                                                      String incrementAmount) {
        RoundingPolicyEntity policy = new RoundingPolicyEntity();
        policy.setStoreLocation(storeLocation);
        policy.setTenderType(tenderType);
        policy.setRoundingMethod(method);
        policy.setIncrementAmount(new BigDecimal(incrementAmount));
        policy.setActive(true);
        return roundingPolicyRepository.save(policy);
    }
}
