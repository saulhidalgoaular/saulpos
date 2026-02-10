package com.saulpos.server.report;

import com.saulpos.api.refund.SaleReturnSubmitLineRequest;
import com.saulpos.api.refund.SaleReturnSubmitRequest;
import com.saulpos.api.sale.SaleCartAddLineRequest;
import com.saulpos.api.sale.SaleCartCreateRequest;
import com.saulpos.api.sale.SaleCartResponse;
import com.saulpos.api.sale.SaleCheckoutPaymentRequest;
import com.saulpos.api.sale.SaleCheckoutRequest;
import com.saulpos.api.sale.SaleCheckoutResponse;
import com.saulpos.api.shift.CashMovementRequest;
import com.saulpos.api.shift.CashShiftCloseRequest;
import com.saulpos.api.shift.CashShiftOpenRequest;
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
import com.saulpos.server.inventory.model.PurchaseOrderEntity;
import com.saulpos.server.inventory.model.PurchaseOrderLineEntity;
import com.saulpos.server.inventory.model.PurchaseOrderStatus;
import com.saulpos.server.inventory.repository.PurchaseOrderRepository;
import com.saulpos.server.sale.service.SaleCartService;
import com.saulpos.server.sale.service.SaleCheckoutService;
import com.saulpos.server.sale.service.SaleReturnService;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.UserAccountRepository;
import com.saulpos.server.supplier.model.SupplierEntity;
import com.saulpos.server.supplier.repository.SupplierRepository;
import com.saulpos.server.tax.model.StoreTaxRuleEntity;
import com.saulpos.server.tax.model.TaxGroupEntity;
import com.saulpos.server.tax.repository.StoreTaxRuleRepository;
import com.saulpos.server.tax.repository.TaxGroupRepository;
import com.saulpos.server.shift.service.ShiftService;
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

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;

    private Long merchantId;
    private Long cashierOneId;
    private Long cashierTwoId;
    private Long storeOneId;
    private Long storeTwoId;
    private Long terminalOneId;
    private Long terminalTwoId;
    private Long categoryOneId;
    private Long categoryTwoId;
    private Long productOneId;
    private Long productTwoId;
    private Long supplierOneId;
    private Long supplierTwoId;

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
        merchantId = merchant.getId();

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
        categoryOneId = categoryA.getId();

        CategoryEntity categoryB = new CategoryEntity();
        categoryB.setMerchant(merchant);
        categoryB.setCode("CAT-L1-B");
        categoryB.setName("Category L1 B");
        categoryB.setActive(true);
        categoryB = categoryRepository.save(categoryB);
        categoryTwoId = categoryB.getId();

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

        supplierOneId = createSupplierForProduct("SUP-L2-A", "Supplier L2 A", storeOneId, productOneId);
        supplierTwoId = createSupplierForProduct("SUP-L2-B", "Supplier L2 B", storeTwoId, productTwoId);
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
    void inventoryStockOnHandLowStockAndMovementReportsSupportFilters() throws Exception {
        insertInventoryMovement(
                storeOneId,
                productOneId,
                "ADJUSTMENT",
                new BigDecimal("12.000"),
                "PURCHASE_RECEIPT",
                "PO-L2-1",
                Instant.parse("2026-02-01T08:00:00Z"));
        insertInventoryMovement(
                storeOneId,
                productOneId,
                "ADJUSTMENT",
                new BigDecimal("-4.000"),
                "STOCK_ADJUSTMENT",
                "ADJ-L2-1",
                Instant.parse("2026-02-02T09:30:00Z"));
        insertInventoryMovement(
                storeTwoId,
                productTwoId,
                "ADJUSTMENT",
                new BigDecimal("3.000"),
                "PURCHASE_RECEIPT",
                "PO-L2-2",
                Instant.parse("2026-02-03T12:00:00Z"));

        insertInventoryCost(storeOneId, productOneId, new BigDecimal("2.5000"), new BigDecimal("2.7000"));
        insertInventoryCost(storeTwoId, productTwoId, new BigDecimal("5.0000"), new BigDecimal("5.2000"));

        mockMvc.perform(get("/api/reports/inventory/stock-on-hand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows.length()").value(2))
                .andExpect(jsonPath("$.rows[0].storeLocationId").value(storeOneId))
                .andExpect(jsonPath("$.rows[0].productId").value(productOneId))
                .andExpect(jsonPath("$.rows[0].quantityOnHand").value(8.000))
                .andExpect(jsonPath("$.rows[0].weightedAverageCost").value(2.5000))
                .andExpect(jsonPath("$.rows[0].stockValue").value(20.00));

        mockMvc.perform(get("/api/reports/inventory/stock-on-hand")
                        .param("supplierId", supplierOneId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].productId").value(productOneId));

        mockMvc.perform(get("/api/reports/inventory/low-stock")
                        .param("minimumQuantity", "5.000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].productId").value(productTwoId))
                .andExpect(jsonPath("$.rows[0].quantityOnHand").value(3.000))
                .andExpect(jsonPath("$.rows[0].minimumQuantity").value(5.000))
                .andExpect(jsonPath("$.rows[0].shortageQuantity").value(2.000));

        mockMvc.perform(get("/api/reports/inventory/movements")
                        .param("from", "2026-02-02T00:00:00Z")
                        .param("to", "2026-02-04T00:00:00Z")
                        .param("categoryId", categoryTwoId.toString())
                        .param("supplierId", supplierTwoId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows.length()").value(1))
                .andExpect(jsonPath("$.rows[0].productId").value(productTwoId))
                .andExpect(jsonPath("$.rows[0].referenceNumber").value("PO-L2-2"));
    }

    @Test
    void inventoryReportsRejectInvalidInputs() throws Exception {
        mockMvc.perform(get("/api/reports/inventory/movements")
                        .param("from", "2026-02-05T00:00:00Z")
                        .param("to", "2026-02-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(get("/api/reports/inventory/low-stock")
                        .param("minimumQuantity", "-1.000"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    @Test
    void cashShiftAndEndOfDayReportsReturnExpectedVsCountedAndVarianceReasons() throws Exception {
        Long shiftOneId = shiftService.openShift(new CashShiftOpenRequest(cashierOneId, terminalOneId, new BigDecimal("100.00"))).id();
        shiftService.addCashMovement(shiftOneId, new CashMovementRequest(
                com.saulpos.api.shift.CashMovementType.PAID_IN,
                new BigDecimal("20.00"),
                "Safe drop replenishment"));
        shiftService.addCashMovement(shiftOneId, new CashMovementRequest(
                com.saulpos.api.shift.CashMovementType.PAID_OUT,
                new BigDecimal("5.00"),
                "Petty cash"));
        shiftService.closeShift(shiftOneId, new CashShiftCloseRequest(new BigDecimal("118.00"), "REGISTER_OVER"));
        setShiftTimestamps(shiftOneId, Instant.parse("2026-02-04T07:00:00Z"), Instant.parse("2026-02-04T16:00:00Z"));

        Long shiftTwoId = shiftService.openShift(new CashShiftOpenRequest(cashierTwoId, terminalTwoId, new BigDecimal("80.00"))).id();
        shiftService.closeShift(shiftTwoId, new CashShiftCloseRequest(new BigDecimal("75.00"), "REGISTER_SHORT"));
        setShiftTimestamps(shiftTwoId, Instant.parse("2026-02-05T07:00:00Z"), Instant.parse("2026-02-05T15:30:00Z"));

        mockMvc.perform(get("/api/reports/cash/shifts")
                        .param("from", "2026-02-01T00:00:00Z")
                        .param("to", "2026-02-06T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.summary.shiftCount").value(2))
                .andExpect(jsonPath("$.summary.closedShiftCount").value(2))
                .andExpect(jsonPath("$.summary.openShiftCount").value(0))
                .andExpect(jsonPath("$.summary.expectedCloseCash").value(195.00))
                .andExpect(jsonPath("$.summary.countedCloseCash").value(193.00))
                .andExpect(jsonPath("$.summary.varianceCash").value(-2.00))
                .andExpect(jsonPath("$.rows.length()").value(2))
                .andExpect(jsonPath("$.rows[0].varianceReason").value("REGISTER_SHORT"))
                .andExpect(jsonPath("$.rows[1].varianceReason").value("REGISTER_OVER"));

        mockMvc.perform(get("/api/reports/cash/end-of-day")
                        .param("from", "2026-02-01T00:00:00Z")
                        .param("to", "2026-02-06T23:59:59Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rows.length()").value(2))
                .andExpect(jsonPath("$.rows[0].businessDate").value("2026-02-04"))
                .andExpect(jsonPath("$.rows[0].storeLocationId").value(storeOneId))
                .andExpect(jsonPath("$.rows[0].shiftCount").value(1))
                .andExpect(jsonPath("$.rows[0].expectedCloseCash").value(115.00))
                .andExpect(jsonPath("$.rows[0].countedCloseCash").value(118.00))
                .andExpect(jsonPath("$.rows[0].varianceCash").value(3.00))
                .andExpect(jsonPath("$.rows[0].varianceReasons[0].reason").value("REGISTER_OVER"))
                .andExpect(jsonPath("$.rows[0].varianceReasons[0].count").value(1))
                .andExpect(jsonPath("$.rows[1].businessDate").value("2026-02-05"))
                .andExpect(jsonPath("$.rows[1].storeLocationId").value(storeTwoId))
                .andExpect(jsonPath("$.rows[1].shiftCount").value(1))
                .andExpect(jsonPath("$.rows[1].expectedCloseCash").value(80.00))
                .andExpect(jsonPath("$.rows[1].countedCloseCash").value(75.00))
                .andExpect(jsonPath("$.rows[1].varianceCash").value(-5.00))
                .andExpect(jsonPath("$.rows[1].varianceReasons[0].reason").value("REGISTER_SHORT"))
                .andExpect(jsonPath("$.rows[1].varianceReasons[0].count").value(1));
    }

    @Test
    void cashReportsRejectInvalidDateRange() throws Exception {
        mockMvc.perform(get("/api/reports/cash/shifts")
                        .param("from", "2026-02-06T00:00:00Z")
                        .param("to", "2026-02-01T00:00:00Z"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(get("/api/reports/cash/end-of-day")
                        .param("from", "2026-02-06T00:00:00Z")
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

        mockMvc.perform(get("/api/reports/inventory/stock-on-hand"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/reports/cash/shifts"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));

        mockMvc.perform(get("/api/reports/cash/end-of-day"))
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

    private void setShiftTimestamps(Long shiftId, Instant openedAt, Instant closedAt) {
        jdbcTemplate.update(
                "UPDATE cash_shift SET opened_at = ?, closed_at = ?, created_at = ?, updated_at = ? WHERE id = ?",
                Timestamp.from(openedAt),
                Timestamp.from(closedAt),
                Timestamp.from(openedAt),
                Timestamp.from(closedAt),
                shiftId);
        jdbcTemplate.update(
                "UPDATE cash_movement SET occurred_at = ?, created_at = ?, updated_at = ? WHERE shift_id = ? AND movement_type = 'OPEN'",
                Timestamp.from(openedAt),
                Timestamp.from(openedAt),
                Timestamp.from(openedAt),
                shiftId);
        jdbcTemplate.update(
                "UPDATE cash_movement SET occurred_at = ?, created_at = ?, updated_at = ? WHERE shift_id = ? AND movement_type = 'CLOSE'",
                Timestamp.from(closedAt),
                Timestamp.from(closedAt),
                Timestamp.from(closedAt),
                shiftId);
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

    private void insertInventoryMovement(Long storeLocationId,
                                         Long productId,
                                         String movementType,
                                         BigDecimal quantityDelta,
                                         String referenceType,
                                         String referenceNumber,
                                         Instant createdAt) {
        jdbcTemplate.update(
                """
                INSERT INTO inventory_movement (
                    store_location_id,
                    product_id,
                    movement_type,
                    quantity_delta,
                    reference_type,
                    reference_number,
                    created_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?)
                """,
                storeLocationId,
                productId,
                movementType,
                quantityDelta,
                referenceType,
                referenceNumber,
                Timestamp.from(createdAt));
    }

    private void insertInventoryCost(Long storeLocationId,
                                     Long productId,
                                     BigDecimal weightedAverageCost,
                                     BigDecimal lastCost) {
        jdbcTemplate.update(
                """
                INSERT INTO inventory_product_cost (
                    store_location_id,
                    product_id,
                    weighted_average_cost,
                    last_cost,
                    updated_at
                ) VALUES (?, ?, ?, ?, ?)
                """,
                storeLocationId,
                productId,
                weightedAverageCost,
                lastCost,
                Timestamp.from(Instant.parse("2026-02-04T00:00:00Z")));
    }

    private Long createSupplierForProduct(String code, String name, Long storeLocationId, Long productId) {
        SupplierEntity supplier = new SupplierEntity();
        supplier.setMerchant(merchantRepository.findById(merchantId).orElseThrow());
        supplier.setCode(code);
        supplier.setName(name);
        supplier.setActive(true);
        supplier = supplierRepository.save(supplier);

        PurchaseOrderEntity purchaseOrder = new PurchaseOrderEntity();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStoreLocation(storeLocationRepository.findById(storeLocationId).orElseThrow());
        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        purchaseOrder.setReferenceNumber("PO-REF-" + code);
        purchaseOrder.setCreatedBy("report-seed");
        purchaseOrder.setCreatedAt(Instant.parse("2026-02-01T00:00:00Z"));
        purchaseOrder.setApprovedBy("report-seed");
        purchaseOrder.setApprovedAt(Instant.parse("2026-02-01T00:05:00Z"));
        purchaseOrder.setLastReceivedBy("report-seed");
        purchaseOrder.setLastReceivedAt(Instant.parse("2026-02-01T00:10:00Z"));

        PurchaseOrderLineEntity line = new PurchaseOrderLineEntity();
        line.setProduct(productRepository.findById(productId).orElseThrow());
        line.setOrderedQuantity(new BigDecimal("100.000"));
        line.setReceivedQuantity(new BigDecimal("100.000"));
        purchaseOrder.addLine(line);

        purchaseOrderRepository.save(purchaseOrder);
        return supplier.getId();
    }

    private record CheckoutSeed(Long cartId, Long cartLineId, Long saleId, String receiptNumber) {
    }
}
