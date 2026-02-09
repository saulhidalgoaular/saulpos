package com.saulpos.server.customer;

import com.saulpos.api.refund.SaleReturnSubmitLineRequest;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCheckoutPaymentRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.api.tax.TaxMode;
import com.saulpos.api.tax.TenderType;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.sale.service.SaleCartService;
import com.saulpos.server.sale.service.SaleCheckoutService;
import com.saulpos.server.sale.service.SaleReturnService;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "sales-agent", authorities = {"PERM_SALES_PROCESS"})
class CustomerHistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private SaleCartService saleCartService;

    @Autowired
    private SaleCheckoutService saleCheckoutService;

    @Autowired
    private SaleReturnService saleReturnService;

    private Long cashierUserId;
    private Long storeLocationId;
    private Long terminalDeviceId;
    private Long productId;
    private Long customerId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM loyalty_event");
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
        jdbcTemplate.execute("DELETE FROM auth_audit_event");
        jdbcTemplate.execute("DELETE FROM auth_session");
        jdbcTemplate.execute("DELETE FROM user_role");
        jdbcTemplate.execute("DELETE FROM role_permission");
        jdbcTemplate.execute("DELETE FROM app_permission");
        jdbcTemplate.execute("DELETE FROM app_role");
        jdbcTemplate.execute("DELETE FROM user_account");

        UserAccountEntity cashier = new UserAccountEntity();
        cashier.setUsername("cashier-history");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashierUserId = userAccountRepository.save(cashier).getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-F3");
        merchant.setName("Merchant F3");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-F3");
        storeLocation.setName("Store F3");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-F3");
        terminal.setName("Terminal F3");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);
        terminalDeviceId = terminal.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-F3");
        category.setName("Category F3");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10-F3");
        taxGroup.setName("VAT 10 F3");
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
        product.setSku("SKU-F3-001");
        product.setName("Product F3");
        product.setBasePrice(new BigDecimal("5.00"));
        product.setDescription("History item");
        product.setActive(true);
        product = productRepository.save(product);
        productId = product.getId();

        CustomerEntity customer = new CustomerEntity();
        customer.setMerchant(merchant);
        customer.setDisplayName("Customer F3");
        customer.setActive(true);
        customer = customerRepository.save(customer);
        customerId = customer.getId();
    }

    @Test
    void customerHistorySupportsPaginationAndDateFilters() throws Exception {
        CheckoutSeed saleOne = checkoutWithCustomerAt(Instant.parse("2026-01-20T10:00:00Z"));
        CheckoutSeed saleTwo = checkoutWithCustomerAt(Instant.parse("2026-01-30T10:00:00Z"));
        CheckoutSeed saleThree = checkoutWithCustomerAt(Instant.parse("2026-02-05T10:00:00Z"));

        createReturnAt(saleOne, "DAMAGED", Instant.parse("2026-01-21T09:00:00Z"));
        Long recentReturnId = createReturnAt(saleThree, "CUSTOMER_RETURN", Instant.parse("2026-02-06T09:00:00Z"));

        mockMvc.perform(get("/api/customers/{id}/sales", customerId)
                        .param("from", "2026-01-25T00:00:00Z")
                        .param("to", "2026-02-05T23:59:59Z")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].saleId").value(saleThree.saleId()))
                .andExpect(jsonPath("$.items[0].receiptNumber").value(saleThree.receiptNumber()))
                .andExpect(jsonPath("$.items[0].totalPayable").value(5.50))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.hasNext").value(true))
                .andExpect(jsonPath("$.hasPrevious").value(false));

        mockMvc.perform(get("/api/customers/{id}/sales", customerId)
                        .param("from", "2026-01-25T00:00:00Z")
                        .param("to", "2026-02-05T23:59:59Z")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].saleId").value(saleTwo.saleId()))
                .andExpect(jsonPath("$.hasNext").value(false))
                .andExpect(jsonPath("$.hasPrevious").value(true));

        mockMvc.perform(get("/api/customers/{id}/returns", customerId)
                        .param("from", "2026-02-01T00:00:00Z")
                        .param("to", "2026-02-10T00:00:00Z")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].saleReturnId").value(recentReturnId))
                .andExpect(jsonPath("$.items[0].saleId").value(saleThree.saleId()))
                .andExpect(jsonPath("$.items[0].receiptNumber").value(saleThree.receiptNumber()))
                .andExpect(jsonPath("$.items[0].totalGross").value(5.50))
                .andExpect(jsonPath("$.totalElements").value(1));

        mockMvc.perform(get("/api/customers/{id}/returns", customerId)
                        .param("from", "2026-02-10T00:00:00Z")
                        .param("to", "2026-02-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    @Test
    @WithMockUser(username = "report-user", authorities = {"PERM_REPORT_VIEW"})
    void customerHistoryRequiresAuthorizedRoles() throws Exception {
        mockMvc.perform(get("/api/customers/{id}/sales", customerId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/customers/{id}/returns", customerId))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    private CheckoutSeed checkoutWithCustomerAt(Instant soldAt) {
        long cartId = saleCartService.createCart(new SaleCartCreateRequest(
                cashierUserId,
                storeLocationId,
                terminalDeviceId,
                soldAt.minusSeconds(60))).id();
        saleCartService.addLine(cartId, new SaleCartAddLineRequest(
                "F3-" + cartId,
                productId,
                new BigDecimal("1.000"),
                null,
                null));

        SaleCheckoutResponse checkoutResponse = saleCheckoutService.checkout(new SaleCheckoutRequest(
                cartId,
                cashierUserId,
                terminalDeviceId,
                List.of(new SaleCheckoutPaymentRequest(
                        TenderType.CASH,
                        new BigDecimal("5.50"),
                        new BigDecimal("10.00"),
                        "F3-PAY-" + cartId)),
                customerId));

        Long saleLineId = jdbcTemplate.queryForObject(
                "SELECT id FROM sale_line WHERE sale_id = ?",
                Long.class,
                checkoutResponse.saleId());

        jdbcTemplate.update(
                "UPDATE sale SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(soldAt),
                Timestamp.from(soldAt),
                checkoutResponse.saleId());

        return new CheckoutSeed(checkoutResponse.saleId(), checkoutResponse.receiptNumber(), saleLineId);
    }

    private Long createReturnAt(CheckoutSeed checkout, String reasonCode, Instant returnedAt) {
        Long saleReturnId = saleReturnService.submit(new SaleReturnSubmitRequest(
                checkout.saleId(),
                null,
                reasonCode,
                TenderType.CASH,
                "RET-" + checkout.saleId(),
                null,
                List.of(new SaleReturnSubmitLineRequest(
                        checkout.saleLineId(),
                        new BigDecimal("1.000"))))).saleReturnId();

        jdbcTemplate.update(
                "UPDATE sale_return SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(returnedAt),
                Timestamp.from(returnedAt),
                saleReturnId);
        return saleReturnId;
    }

    private record CheckoutSeed(Long saleId, String receiptNumber, Long saleLineId) {
    }
}
