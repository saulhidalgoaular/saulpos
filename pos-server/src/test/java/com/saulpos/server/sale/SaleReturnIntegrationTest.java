package com.saulpos.server.sale;

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
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.model.TerminalDeviceEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.identity.repository.TerminalDeviceRepository;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import com.saulpos.server.sale.service.SaleCartService;
import com.saulpos.server.sale.service.SaleCheckoutService;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "refund-agent", authorities = {"PERM_REFUND_PROCESS"})
class SaleReturnIntegrationTest {

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
    private SaleCartService saleCartService;

    @Autowired
    private SaleCheckoutService saleCheckoutService;

    private Long cashierUserId;
    private Long storeLocationId;
    private Long terminalDeviceId;
    private Long productId;

    @BeforeEach
    void setUp() {
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
        cashier.setUsername("cashier-return");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashierUserId = userAccountRepository.save(cashier).getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-G3");
        merchant.setName("Merchant G3");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-G3");
        storeLocation.setName("Store G3");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-G3");
        terminal.setName("Terminal G3");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);
        terminalDeviceId = terminal.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-G3");
        category.setName("Category G3");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10-G3");
        taxGroup.setName("VAT 10 G3");
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
        product.setSku("SKU-G3-001");
        product.setName("Product G3");
        product.setBasePrice(new BigDecimal("5.00"));
        product.setDescription("Returns item");
        product.setActive(true);
        product = productRepository.save(product);
        productId = product.getId();
    }

    @Test
    void returnFlowSupportsLookupPartialAndFullReturn() throws Exception {
        CheckoutResult checkout = checkoutWithTwoUnits();

        mockMvc.perform(get("/api/refunds/lookup")
                        .param("receiptNumber", checkout.receiptNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").value(checkout.saleId()))
                .andExpect(jsonPath("$.lines.length()").value(1))
                .andExpect(jsonPath("$.lines[0].quantitySold").value(2.000))
                .andExpect(jsonPath("$.lines[0].quantityReturned").value(0.000))
                .andExpect(jsonPath("$.lines[0].quantityAvailable").value(2.000));

        mockMvc.perform(post("/api/refunds/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiptNumber": "%s",
                                  "reasonCode": "DAMAGED",
                                  "refundTenderType": "CASH",
                                  "refundReference": "REF-G3-1",
                                  "lines": [
                                    {
                                      "saleLineId": %d,
                                      "quantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(checkout.receiptNumber(), checkout.saleLineId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").value(checkout.saleId()))
                .andExpect(jsonPath("$.totalGross").value(5.50))
                .andExpect(jsonPath("$.lines[0].quantity").value(1.000));

        mockMvc.perform(get("/api/refunds/lookup")
                        .param("receiptNumber", checkout.receiptNumber()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].quantityReturned").value(1.000))
                .andExpect(jsonPath("$.lines[0].quantityAvailable").value(1.000));

        mockMvc.perform(post("/api/refunds/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiptNumber": "%s",
                                  "reasonCode": "CUSTOMER_RETURN",
                                  "refundTenderType": "CARD",
                                  "refundReference": "AUTH-R2",
                                  "lines": [
                                    {
                                      "saleLineId": %d,
                                      "quantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(checkout.receiptNumber(), checkout.saleLineId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGross").value(5.50));

        mockMvc.perform(post("/api/refunds/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiptNumber": "%s",
                                  "reasonCode": "EXCESS_ATTEMPT",
                                  "refundTenderType": "CASH",
                                  "lines": [
                                    {
                                      "saleLineId": %d,
                                      "quantity": 0.500
                                    }
                                  ]
                                }
                                """.formatted(checkout.receiptNumber(), checkout.saleLineId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        Integer returnCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sale_return WHERE sale_id = ?",
                Integer.class,
                checkout.saleId());
        BigDecimal returnQtyTotal = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(quantity_delta), 0) FROM inventory_movement WHERE movement_type = 'RETURN' AND reference_type = 'SALE_RETURN'",
                BigDecimal.class);

        assertThat(returnCount).isEqualTo(2);
        assertThat(returnQtyTotal).isEqualByComparingTo(new BigDecimal("2.000"));
    }

    @Test
    void submitReturnOutsideWindowRequiresManagerApproval() throws Exception {
        CheckoutResult checkout = checkoutWithTwoUnits();
        makeSaleOld(checkout.saleId(), Instant.now().minusSeconds(60L * 24L * 60L * 60L));

        mockMvc.perform(post("/api/refunds/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": %d,
                                  "reasonCode": "LATE_RETURN",
                                  "refundTenderType": "CASH",
                                  "lines": [
                                    {
                                      "saleLineId": %d,
                                      "quantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(checkout.saleId(), checkout.saleLineId())))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"PERM_CONFIGURATION_MANAGE"})
    void managerCanSubmitReturnOutsideWindow() throws Exception {
        CheckoutResult checkout = checkoutWithTwoUnits();
        makeSaleOld(checkout.saleId(), Instant.now().minusSeconds(60L * 24L * 60L * 60L));

        mockMvc.perform(post("/api/refunds/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": %d,
                                  "reasonCode": "LATE_MANAGER_APPROVED",
                                  "refundTenderType": "CASH",
                                  "lines": [
                                    {
                                      "saleLineId": %d,
                                      "quantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(checkout.saleId(), checkout.saleLineId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalGross").value(5.50));
    }

    private CheckoutResult checkoutWithTwoUnits() throws Exception {
        long cartId = createCart();
        addLine(cartId, 2);

        SaleCheckoutResponse checkoutResponse = saleCheckoutService.checkout(new SaleCheckoutRequest(
                cartId,
                cashierUserId,
                terminalDeviceId,
                List.of(new SaleCheckoutPaymentRequest(
                        TenderType.CASH,
                        new BigDecimal("11.00"),
                        new BigDecimal("11.00"),
                        null))));
        long saleId = checkoutResponse.saleId();
        String receiptNumber = checkoutResponse.receiptNumber();
        Long saleLineId = jdbcTemplate.queryForObject(
                "SELECT id FROM sale_line WHERE sale_id = ? ORDER BY line_number ASC LIMIT 1",
                Long.class,
                saleId);
        return new CheckoutResult(saleId, receiptNumber, saleLineId);
    }

    private long createCart() {
        return saleCartService.createCart(new SaleCartCreateRequest(
                cashierUserId,
                storeLocationId,
                terminalDeviceId,
                Instant.parse("2026-02-10T12:00:00Z"))).id();
    }

    private void addLine(long cartId, int quantity) {
        saleCartService.addLine(cartId, new SaleCartAddLineRequest(
                "scan-g3-1",
                productId,
                new BigDecimal(quantity + ".000"),
                null,
                null));
    }

    private void makeSaleOld(Long saleId, Instant createdAt) {
        jdbcTemplate.update(
                "UPDATE sale SET created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(createdAt),
                Timestamp.from(createdAt),
                saleId);
    }

    private record CheckoutResult(Long saleId, String receiptNumber, Long saleLineId) {
    }
}
