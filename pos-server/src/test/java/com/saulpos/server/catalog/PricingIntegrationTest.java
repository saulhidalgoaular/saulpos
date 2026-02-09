package com.saulpos.server.catalog;

import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.PriceBookEntity;
import com.saulpos.server.catalog.model.PriceBookItemEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.model.StorePriceOverrideEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.PriceBookItemRepository;
import com.saulpos.server.catalog.repository.PriceBookRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.catalog.repository.StorePriceOverrideRepository;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.model.CustomerGroupAssignmentEntity;
import com.saulpos.server.customer.model.CustomerGroupEntity;
import com.saulpos.server.customer.repository.CustomerGroupRepository;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class PricingIntegrationTest {

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
    private PriceBookRepository priceBookRepository;

    @Autowired
    private PriceBookItemRepository priceBookItemRepository;

    @Autowired
    private StorePriceOverrideRepository storePriceOverrideRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerGroupRepository customerGroupRepository;

    private Long merchantId;
    private Long storeLocationId;
    private Long productId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
        jdbcTemplate.execute("DELETE FROM customer_group_assignment");
        jdbcTemplate.execute("DELETE FROM customer_group");
        jdbcTemplate.execute("DELETE FROM customer_contact");
        jdbcTemplate.execute("DELETE FROM customer_tax_identity");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM cash_movement");
        jdbcTemplate.execute("DELETE FROM cash_shift");
        jdbcTemplate.execute("DELETE FROM store_price_override");
        jdbcTemplate.execute("DELETE FROM price_book_item");
        jdbcTemplate.execute("DELETE FROM price_book");
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
        merchant.setCode("MER-C3");
        merchant.setName("Merchant C3");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);
        merchantId = merchant.getId();

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-C3");
        storeLocation.setName("Store C3");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-C3");
        category.setName("Category C3");
        category.setActive(true);
        category = categoryRepository.save(category);

        ProductEntity product = new ProductEntity();
        product.setMerchant(merchant);
        product.setCategory(category);
        product.setSku("SKU-C3-001");
        product.setName("Product C3");
        product.setBasePrice(new BigDecimal("11.50"));
        product.setDescription("base");
        product.setActive(true);
        product = productRepository.save(product);
        productId = product.getId();
    }

    @Test
    void resolveUsesStoreOverrideBeforePriceBookAndBasePrice() throws Exception {
        Instant at = Instant.parse("2026-02-01T12:00:00Z");
        createPriceBookWithItem("PB-C3-01", "Book 1", new BigDecimal("9.20"), at.minusSeconds(3600), null, true, null);
        StorePriceOverrideEntity override = createStoreOverride(new BigDecimal("8.35"), at.minusSeconds(1200), null, true);

        mockMvc.perform(get("/api/catalog/prices/resolve")
                        .param("storeLocationId", storeLocationId.toString())
                        .param("productId", productId.toString())
                        .param("at", at.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("STORE_OVERRIDE"))
                .andExpect(jsonPath("$.resolvedPrice").value(8.35))
                .andExpect(jsonPath("$.sourceId").value(override.getId()));
    }

    @Test
    void resolveHonorsPriceBookEffectiveDateWindows() throws Exception {
        createPriceBookWithItem(
                "PB-C3-PAST",
                "Past Book",
                new BigDecimal("7.10"),
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-01-31T23:59:59Z"),
                true,
                null);
        createPriceBookWithItem(
                "PB-C3-CURRENT",
                "Current Book",
                new BigDecimal("9.45"),
                Instant.parse("2026-02-01T00:00:00Z"),
                null,
                true,
                null);

        mockMvc.perform(get("/api/catalog/prices/resolve")
                        .param("storeLocationId", storeLocationId.toString())
                        .param("productId", productId.toString())
                        .param("at", "2026-01-15T12:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("PRICE_BOOK"))
                .andExpect(jsonPath("$.resolvedPrice").value(7.10));

        mockMvc.perform(get("/api/catalog/prices/resolve")
                        .param("storeLocationId", storeLocationId.toString())
                        .param("productId", productId.toString())
                        .param("at", "2026-03-01T12:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("PRICE_BOOK"))
                .andExpect(jsonPath("$.resolvedPrice").value(9.45));
    }

    @Test
    void resolveFallsBackToBasePriceWhenNoApplicableOverrideOrPriceBook() throws Exception {
        createPriceBookWithItem(
                "PB-C3-FUTURE",
                "Future Book",
                new BigDecimal("5.00"),
                Instant.parse("2027-01-01T00:00:00Z"),
                null,
                true,
                null);
        createStoreOverride(
                new BigDecimal("4.99"),
                Instant.parse("2027-01-01T00:00:00Z"),
                null,
                true);

        mockMvc.perform(get("/api/catalog/prices/resolve")
                        .param("storeLocationId", storeLocationId.toString())
                        .param("productId", productId.toString())
                        .param("at", "2026-06-01T12:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("BASE_PRICE"))
                .andExpect(jsonPath("$.resolvedPrice").value(11.50))
                .andExpect(jsonPath("$.sourceId").value(productId));
    }

    @Test
    void resolveUsesCustomerGroupPriceBookWhenCustomerContextIsProvided() throws Exception {
        CustomerEntity customer = createCustomer("Customer Group Buyer");
        CustomerGroupEntity wholesaleGroup = createCustomerGroup("WHOLESALE", "Wholesale Group");
        assignCustomerToGroup(customer, wholesaleGroup);

        createPriceBookWithItem(
                "PB-C3-GENERAL",
                "General Book",
                new BigDecimal("9.90"),
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                true,
                null);
        createPriceBookWithItem(
                "PB-C3-WHOLESALE",
                "Wholesale Book",
                new BigDecimal("7.25"),
                Instant.parse("2026-01-01T00:00:00Z"),
                null,
                true,
                wholesaleGroup.getId());

        mockMvc.perform(get("/api/catalog/prices/resolve")
                        .param("storeLocationId", storeLocationId.toString())
                        .param("productId", productId.toString())
                        .param("customerId", customer.getId().toString())
                        .param("at", "2026-04-01T12:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.source").value("CUSTOMER_GROUP_PRICE_BOOK"))
                .andExpect(jsonPath("$.resolvedPrice").value(7.25));
    }

    private void createPriceBookWithItem(String code,
                                         String name,
                                         BigDecimal price,
                                         Instant effectiveFrom,
                                         Instant effectiveTo,
                                         boolean active,
                                         Long customerGroupId) {
        PriceBookEntity priceBook = new PriceBookEntity();
        priceBook.setMerchant(merchantRepository.getReferenceById(merchantId));
        priceBook.setCode(code);
        priceBook.setName(name);
        priceBook.setEffectiveFrom(effectiveFrom);
        priceBook.setEffectiveTo(effectiveTo);
        priceBook.setActive(active);
        if (customerGroupId != null) {
            priceBook.setCustomerGroup(customerGroupRepository.getReferenceById(customerGroupId));
        }
        priceBook = priceBookRepository.save(priceBook);

        PriceBookItemEntity item = new PriceBookItemEntity();
        item.setPriceBook(priceBook);
        item.setProduct(productRepository.getReferenceById(productId));
        item.setPrice(price);
        priceBookItemRepository.save(item);
    }

    private StorePriceOverrideEntity createStoreOverride(BigDecimal price,
                                                         Instant effectiveFrom,
                                                         Instant effectiveTo,
                                                         boolean active) {
        StorePriceOverrideEntity override = new StorePriceOverrideEntity();
        override.setStoreLocation(storeLocationRepository.getReferenceById(storeLocationId));
        override.setProduct(productRepository.getReferenceById(productId));
        override.setPrice(price);
        override.setEffectiveFrom(effectiveFrom);
        override.setEffectiveTo(effectiveTo);
        override.setActive(active);
        return storePriceOverrideRepository.save(override);
    }

    private CustomerEntity createCustomer(String displayName) {
        CustomerEntity customer = new CustomerEntity();
        customer.setMerchant(merchantRepository.getReferenceById(merchantId));
        customer.setDisplayName(displayName);
        customer.setActive(true);
        return customerRepository.save(customer);
    }

    private CustomerGroupEntity createCustomerGroup(String code, String name) {
        CustomerGroupEntity customerGroup = new CustomerGroupEntity();
        customerGroup.setMerchant(merchantRepository.getReferenceById(merchantId));
        customerGroup.setCode(code);
        customerGroup.setName(name);
        customerGroup.setActive(true);
        return customerGroupRepository.save(customerGroup);
    }

    private void assignCustomerToGroup(CustomerEntity customer, CustomerGroupEntity customerGroup) {
        CustomerGroupAssignmentEntity assignment = new CustomerGroupAssignmentEntity();
        assignment.setCustomerGroup(customerGroup);
        assignment.setActive(true);
        customer.addGroupAssignment(assignment);
        customerRepository.save(customer);
    }
}
