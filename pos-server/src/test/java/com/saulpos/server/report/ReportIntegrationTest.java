package com.saulpos.server.report;

import com.saulpos.api.refund.SaleReturnSubmitLineRequest;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartResponse;
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
@WithMockUser(username = "report-agent", authorities = {"PERM_REPORT_VIEW"})
class ReportIntegrationTest {

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

    @Autowired
    private SaleReturnService saleReturnService;

    private Long cashierOneId;
    private Long cashierTwoId;
    private Long storeOneId;
    private Long storeTwoId;
    private Long terminalOneId;
    private Long terminalTwoId;
    private Long productOneId;
    private Long productTwoId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM loyalty_event");
        jdbcTemplate.execute("DELETE FROM sale_return_refund");
        jdbcTemplate.execute("DELETE FROM sale_return_line");
        jdbcTemplate.execute("DELETE FROM sale_return");
        jdbcTemplate.execute("DELETE FROM payment_transition");
        jdbcTemplate.execute("DELETE FROM payment_allocation");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM inventory_movement_lot");
        jdbcTemplate.execute("DELETE FROM inventory_lot_balance");
        jdbcTemplate.execute("DELETE FROM inventory_lot");
        jdbcTemplate.execute("DELETE FROM inventory_product_cost");
        jdbcTemplate.execute("DELETE FROM inventory_movement");
        jdbcTemplate.execute("DELETE FROM sale_line");
        jdbcTemplate.execute("DELETE FROM sale");
        jdbcTemplate.execute("DELETE FROM sale_override_event");
        jdbcTemplate.execute("DELETE FROM void_reason_code");
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
        jdbcTemplate.execute("DELETE FROM goods_receipt");
        jdbcTemplate.execute("DELETE FROM purchase_order_line");
        jdbcTemplate.execute("DELETE FROM purchase_order");
        jdbcTemplate.execute("DELETE FROM supplier_return_line");
        jdbcTemplate.execute("DELETE FROM supplier_return");
        jdbcTemplate.execute("DELETE FROM stocktake_line");
        jdbcTemplate.execute("DELETE FROM stocktake_session");
        jdbcTemplate.execute("DELETE FROM stock_adjustment");
        jdbcTemplate.execute("DELETE FROM stock_transfer_line");
        jdbcTemplate.execute("DELETE FROM stock_transfer");
        jdbcTemplate.execute("DELETE FROM supplier_terms");
        jdbcTemplate.execute("DELETE FROM supplier_contact");
        jdbcTemplate.execute("DELETE FROM supplier");
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

        UserAccountEntity cashierOne = new UserAccountEntity();
        cashierOne.setUsername("cashier-report-1");
        cashierOne.setPasswordHash("hash");
        cashierOne.setActive(true);
        cashierOneId = userAccountRepository.save(cashierOne).getId();

        UserAccountEntity cashierTwo = new UserAccountEntity();
        cashierTwo.setUsername("cashier-report-2");
        cashierTwo.setPasswordHash("hash");
        cashierTwo.setActive(true);
        cashierTwoId = userAccountRepository.save(cashierTwo).getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-L1");
        merchant.setName("Merchant L1");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeOne = new StoreLocationEntity();
        storeOne.setMerchant(merchant);
        storeOne.setCode("STORE-L1-A");
        storeOne.setName("Store L1 A");
        storeOne.setActive(true);
        storeOne = storeLocationRepository.save(storeOne);
        storeOneId = storeOne.getId();

        StoreLocationEntity storeTwo = new StoreLocationEntity();
        storeTwo.setMerchant(merchant);
        storeTwo.setCode("STORE-L1-B");
        storeTwo.setName("Store L1 B");
        storeTwo.setActive(true);
        storeTwo = storeLocationRepository.save(storeTwo);
        storeTwoId = storeTwo.getId();

        TerminalDeviceEntity terminalOne = new TerminalDeviceEntity();
        terminalOne.setStoreLocation(storeOne);
        terminalOne.setCode("TERM-L1-A");
        terminalOne.setName("Terminal L1 A");
        terminalOne.setActive(true);
        terminalOne = terminalDeviceRepository.save(terminalOne);
        terminalOneId = terminalOne.getId();

        TerminalDeviceEntity terminalTwo = new TerminalDeviceEntity();
        terminalTwo.setStoreLocation(storeTwo);
        terminalTwo.setCode("TERM-L1-B");
        terminalTwo.setName("Terminal L1 B");
        terminalTwo.setActive(true);
        terminalTwo = terminalDeviceRepository.save(terminalTwo);
        terminalTwoId = terminalTwo.getId();

        CategoryEntity categoryA = new CategoryEntity();
        categoryA.setMerchant(merchant);
        categoryA.setCode("CAT-L1-A");
        categoryA.setName("Category L1 A");
        categoryA.setActive(true);
        categoryA = categoryRepository.save(categoryA);

        CategoryEntity categoryB = new CategoryEntity();
        categoryB.setMerchant(merchant);
        categoryB.setCode("CAT-L1-B");
        categoryB.setName("Category L1 B");
        categoryB.setActive(true);
        categoryB = categoryRepository.save(categoryB);

        TaxGroupEntity vat10 = new TaxGroupEntity();
        vat10.setMerchant(merchant);
        vat10.setCode("VAT10-L1");
        vat10.setName("VAT 10 L1");
        vat10.setTaxRatePercent(new BigDecimal("10.0000"));
        vat10.setZeroRated(false);
        vat10.setActive(true);
        vat10 = taxGroupRepository.save(vat10);

        TaxGroupEntity vat0 = new TaxGroupEntity();
        vat0.setMerchant(merchant);
        vat0.setCode("VAT0-L1");
        vat0.setName("VAT 0 L1");
        vat0.setTaxRatePercent(new BigDecimal("0.0000"));
        vat0.setZeroRated(true);
        vat0.setActive(true);
        vat0 = taxGroupRepository.save(vat0);

        createTaxRule(storeOne, vat10, TaxMode.EXCLUSIVE);
        createTaxRule(storeTwo, vat0, TaxMode.EXCLUSIVE);

        ProductEntity productOne = new ProductEntity();
        productOne.setMerchant(merchant);
        productOne.setCategory(categoryA);
        productOne.setTaxGroup(vat10);
        productOne.setSku("SKU-L1-001");
        productOne.setName("Product L1 A");
        productOne.setBasePrice(new BigDecimal("5.00"));
        productOne.setActive(true);
        productOne = productRepository.save(productOne);
        productOneId = productOne.getId();

        ProductEntity productTwo = new ProductEntity();
        productTwo.setMerchant(merchant);
        productTwo.setCategory(categoryB);
        productTwo.setTaxGroup(vat0);
        productTwo.setSku("SKU-L1-002");
        productTwo.setName("Product L1 B");
        productTwo.setBasePrice(new BigDecimal("8.00"));
        productTwo.setActive(true);
        productTwo = productRepository.save(productTwo);
        productTwoId = productTwo.getId();
    }

    @Test
    void salesAndReturnsReportAggregatesByRequiredDimensions() throws Exception {
        CheckoutSeed saleOne = checkout(cashierOneId, storeOneId, terminalOneId, productOneId, new BigDecimal("2.000"));
        CheckoutSeed saleTwo = checkout(cashierTwoId, storeTwoId, terminalTwoId, productTwoId, new BigDecimal("1.000"));

        setSaleTimestamp(saleOne.saleId(), Instant.parse("2026-02-01T10:00:00Z"));
        setSaleTimestamp(saleTwo.saleId(), Instant.parse("2026-02-02T11:00:00Z"));

        Long saleOneLineId = jdbcTemplate.queryForObject(
                "SELECT id FROM sale_line WHERE sale_id = ? ORDER BY id LIMIT 1",
                Long.class,
                saleOne.saleId());

        Long saleReturnId = saleReturnService.submit(new SaleReturnSubmitRequest(
                null,
                saleOne.receiptNumber(),
                "CUSTOMER_REQUEST",
                TenderType.CASH,
                "RET-L1-1",
                null,
                List.of(new SaleReturnSubmitLineRequest(saleOneLineId, new BigDecimal("1.000")))))
                .saleReturnId();

        setReturnTimestamp(saleReturnId, Instant.parse("2026-02-03T09:00:00Z"));
        insertPriceOverrideEvent(saleOne.saleId(), saleOne.cartLineId(), Instant.parse("2026-02-01T09:59:00Z"));

        mockMvc.perform(get("/api/reports/sales")
                        .param("from", "2026-02-01T00:00:00Z")
                        .param("to", "2026-02-05T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.saleCount").value(2))
                .andExpect(jsonPath("$.summary.returnCount").value(1))
                .andExpect(jsonPath("$.summary.salesGross").value(19.00))
                .andExpect(jsonPath("$.summary.returnGross").value(5.50))
                .andExpect(jsonPath("$.summary.netGross").value(13.50))
                .andExpect(jsonPath("$.summary.discountGross").value(2.00))
                .andExpect(jsonPath("$.byDay.length()").value(3))
                .andExpect(jsonPath("$.byStore.length()").value(2))
                .andExpect(jsonPath("$.byTerminal.length()").value(2))
                .andExpect(jsonPath("$.byCashier.length()").value(2))
                .andExpect(jsonPath("$.byCategory.length()").value(2))
                .andExpect(jsonPath("$.byTaxGroup.length()").value(2));

        mockMvc.perform(get("/api/reports/sales")
                        .param("storeLocationId", storeOneId.toString())
                        .param("from", "2026-02-01T00:00:00Z")
                        .param("to", "2026-02-05T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.saleCount").value(1))
                .andExpect(jsonPath("$.summary.returnCount").value(1))
                .andExpect(jsonPath("$.summary.salesGross").value(11.00))
                .andExpect(jsonPath("$.summary.returnGross").value(5.50))
                .andExpect(jsonPath("$.summary.discountGross").value(2.00));
    }

    @Test
    void salesAndReturnsReportRejectsInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/reports/sales")
                        .param("from", "2026-02-05T00:00:00Z")
                        .param("to", "2026-02-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    @Test
    @WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
    void salesAndReturnsReportRequiresReportPermission() throws Exception {
        mockMvc.perform(get("/api/reports/sales"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    private void createTaxRule(StoreLocationEntity storeLocation, TaxGroupEntity taxGroup, TaxMode mode) {
        StoreTaxRuleEntity taxRule = new StoreTaxRuleEntity();
        taxRule.setStoreLocation(storeLocation);
        taxRule.setTaxGroup(taxGroup);
        taxRule.setTaxMode(mode);
        taxRule.setExempt(false);
        taxRule.setActive(true);
        storeTaxRuleRepository.save(taxRule);
    }

    private CheckoutSeed checkout(Long cashierId, Long storeId, Long terminalId, Long productId, BigDecimal quantity) {
        SaleCartResponse cart = saleCartService.createCart(new SaleCartCreateRequest(cashierId, storeId, terminalId, Instant.now()));
        SaleCartResponse cartWithLine = saleCartService.addLine(cart.id(), new SaleCartAddLineRequest(null, productId, quantity, null, null));
        Long cartLineId = cartWithLine.lines().getFirst().lineId();

        SaleCheckoutResponse checkout = saleCheckoutService.checkout(new SaleCheckoutRequest(
                cart.id(),
                cashierId,
                terminalId,
                List.of(new SaleCheckoutPaymentRequest(
                        TenderType.CASH,
                        cartWithLine.totalPayable(),
                        cartWithLine.totalPayable(),
                        null)),
                null));

        return new CheckoutSeed(cart.id(), cartLineId, checkout.saleId(), checkout.receiptNumber());
    }

    private void setSaleTimestamp(Long saleId, Instant timestamp) {
        Timestamp ts = Timestamp.from(timestamp);
        jdbcTemplate.update("UPDATE sale SET created_at = ?, updated_at = ? WHERE id = ?", ts, ts, saleId);
        jdbcTemplate.update("UPDATE sale_line SET created_at = ?, updated_at = ? WHERE sale_id = ?", ts, ts, saleId);
    }

    private void setReturnTimestamp(Long saleReturnId, Instant timestamp) {
        Timestamp ts = Timestamp.from(timestamp);
        jdbcTemplate.update("UPDATE sale_return SET created_at = ?, updated_at = ? WHERE id = ?", ts, ts, saleReturnId);
        jdbcTemplate.update("UPDATE sale_return_line SET created_at = ?, updated_at = ? WHERE sale_return_id = ?", ts, ts, saleReturnId);
    }

    private void insertPriceOverrideEvent(Long saleId, Long lineId, Instant timestamp) {
        Long cartId = jdbcTemplate.queryForObject("SELECT cart_id FROM sale WHERE id = ?", Long.class, saleId);
        jdbcTemplate.update(
                """
                INSERT INTO sale_override_event (
                    cart_id,
                    line_id,
                    event_type,
                    reason_code,
                    before_unit_price,
                    after_unit_price,
                    approval_required,
                    created_at
                ) VALUES (?, ?, 'PRICE_OVERRIDE', 'PRICE_MATCH', ?, ?, FALSE, ?)
                """,
                cartId,
                lineId,
                new BigDecimal("6.00"),
                new BigDecimal("5.00"),
                Timestamp.from(timestamp));
    }

    private record CheckoutSeed(Long cartId, Long cartLineId, Long saleId, String receiptNumber) {
    }
}
