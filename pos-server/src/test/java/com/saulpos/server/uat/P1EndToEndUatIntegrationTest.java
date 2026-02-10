package com.saulpos.server.uat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.api.tax.TaxMode;
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
import com.saulpos.server.supplier.model.SupplierEntity;
import com.saulpos.server.supplier.repository.SupplierRepository;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class P1EndToEndUatIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    private SupplierRepository supplierRepository;

    @BeforeEach
    void setUp() {
        List<String> tables = List.of(
                "loyalty_event",
                "fiscal_event",
                "fiscal_document",
                "receipt_print_event",
                "no_sale_drawer_event",
                "sale_return_refund",
                "sale_return_line",
                "sale_return",
                "payment_transition",
                "payment_allocation",
                "payment",
                "inventory_movement_lot",
                "inventory_lot_balance",
                "inventory_lot",
                "inventory_product_cost",
                "inventory_movement",
                "sale_line",
                "sale",
                "sale_override_event",
                "void_reason_code",
                "sale_cart_event",
                "parked_cart_reference",
                "sale_cart_line",
                "sale_cart",
                "goods_receipt",
                "purchase_order_line",
                "purchase_order",
                "supplier_return_line",
                "supplier_return",
                "stocktake_line",
                "stocktake_session",
                "stock_adjustment",
                "stock_transfer_line",
                "stock_transfer",
                "customer_group_assignment",
                "customer_group",
                "customer_contact",
                "customer_tax_identity",
                "customer",
                "promotion_window",
                "promotion_rule",
                "promotion",
                "discount_application",
                "discount_reason_code",
                "receipt_header",
                "receipt_sequence",
                "receipt_series",
                "rounding_policy",
                "store_tax_rule",
                "tax_group",
                "open_price_entry_audit",
                "store_price_override",
                "price_book_item",
                "price_book",
                "product_barcode",
                "product_variant",
                "product",
                "category",
                "cash_movement",
                "cash_shift",
                "supplier_contact",
                "supplier_terms",
                "supplier",
                "store_user_assignment",
                "terminal_device",
                "store_location",
                "merchant",
                "auth_audit_event",
                "auth_session",
                "user_role",
                "role_permission",
                "app_permission",
                "app_role",
                "idempotency_key_event",
                "user_account");

        for (String table : tables) {
            jdbcTemplate.execute("DELETE FROM " + table);
        }
    }

    @Test
    @WithMockUser(username = "uat-cashier", authorities = {"PERM_SALES_PROCESS"})
    void cashierPersonaCompletesShiftCartCheckoutAndCloseFlow() throws Exception {
        Long cashierUserId = createUser("uat-cashier-1");
        MerchantEntity merchant = createMerchant("MER-UAT-CASHIER", "UAT Cashier Merchant");
        StoreLocationEntity store = createStore(merchant, "STORE-UAT-CASHIER", "UAT Cashier Store");
        TerminalDeviceEntity terminal = createTerminal(store, "TERM-UAT-CASHIER", "UAT Cashier Terminal");
        Long productId = createProduct(merchant, store, "SKU-UAT-CASHIER", "UAT Cashier Product", "5.00");

        long shiftId = openShift(cashierUserId, terminal.getId(), "100.00");
        long cartId = createCart(cashierUserId, store.getId(), terminal.getId());

        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "uat-cashier-line-1",
                                  "productId": %d,
                                  "quantity": 2
                                }
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayable").value(11.00));

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "uat-cashier-checkout-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CASH",
                                      "amount": 11.00,
                                      "tenderedAmount": 11.00
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminal.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiptNumber").isString())
                .andExpect(jsonPath("$.paymentStatus").isString());

        mockMvc.perform(post("/api/shifts/{id}/close", shiftId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "countedCash": 111.00,
                                  "note": "UAT cashier close"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));
    }

    @Test
    @WithMockUser(username = "uat-manager", authorities = {"PERM_SALES_PROCESS", "PERM_DISCOUNT_OVERRIDE"})
    void managerPersonaApprovesHighOverrideThenCompletesCheckout() throws Exception {
        Long cashierUserId = createUser("uat-manager-cashier");
        MerchantEntity merchant = createMerchant("MER-UAT-MANAGER", "UAT Manager Merchant");
        StoreLocationEntity store = createStore(merchant, "STORE-UAT-MANAGER", "UAT Manager Store");
        TerminalDeviceEntity terminal = createTerminal(store, "TERM-UAT-MANAGER", "UAT Manager Terminal");
        seedVoidReasonCodes();
        Long productId = createProduct(merchant, store, "SKU-UAT-MANAGER", "UAT Manager Product", "5.00");

        long cartId = createCart(cashierUserId, store.getId(), terminal.getId());

        MvcResult addLineResult = mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(productId)))
                .andExpect(status().isOk())
                .andReturn();

        long lineId = objectMapper.readTree(addLineResult.getResponse().getContentAsString())
                .path("lines")
                .get(0)
                .path("lineId")
                .asLong();

        mockMvc.perform(post("/api/sales/carts/{id}/lines/{lineId}/price-override", cartId, lineId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "unitPrice": 1.00,
                                  "reasonCode": "PRICE_MATCH",
                                  "note": "manager-authorized markdown"
                                }
                                """.formatted(cashierUserId, terminal.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lines[0].unitPrice").value(1.00))
                .andExpect(jsonPath("$.totalPayable").value(1.10));

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "uat-manager-checkout-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CASH",
                                      "amount": 1.10,
                                      "tenderedAmount": 1.10
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminal.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPayable").value(1.10))
                .andExpect(jsonPath("$.receiptNumber").isString());
    }

    @Test
    @WithMockUser(username = "uat-inventory", authorities = {"PERM_INVENTORY_ADJUST"})
    void inventoryClerkPersonaApprovesAndReceivesPurchaseOrder() throws Exception {
        MerchantEntity merchant = createMerchant("MER-UAT-INV", "UAT Inventory Merchant");
        StoreLocationEntity store = createStore(merchant, "STORE-UAT-INV", "UAT Inventory Store");
        CategoryEntity category = createCategory(merchant, "CAT-UAT-INV", "UAT Inventory Category");
        TaxGroupEntity taxGroup = createTaxGroup(merchant, "VAT-SKU-UAT-INV");
        createStoreTaxRule(store, taxGroup);
        ProductEntity product = createSimpleProduct(
                merchant,
                category,
                taxGroup,
                "SKU-UAT-INV",
                "UAT Inventory Product",
                "3.50");
        SupplierEntity supplier = createSupplier(merchant, "SUP-UAT-INV", "UAT Inventory Supplier");

        MvcResult createPoResult = mockMvc.perform(post("/api/inventory/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": %d,
                                  "storeLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "orderedQuantity": 10.000
                                    }
                                  ]
                                }
                                """.formatted(supplier.getId(), store.getId(), product.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        long purchaseOrderId = objectMapper.readTree(createPoResult.getResponse().getContentAsString())
                .path("id")
                .asLong();

        mockMvc.perform(post("/api/inventory/purchase-orders/{id}/approve", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "UAT approval"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/api/inventory/purchase-orders/{id}/receive", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "receivedQuantity": 10.000,
                                      "unitCost": 3.5000
                                    }
                                  ],
                                  "note": "UAT full receive"
                                }
                                """.formatted(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));

        mockMvc.perform(get("/api/inventory/balances")
                        .param("storeLocationId", String.valueOf(store.getId()))
                        .param("productId", String.valueOf(product.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantityOnHand").value(10.000));
    }

    @Test
    @WithMockUser(username = "uat-admin", authorities = {"PERM_CONFIGURATION_MANAGE"})
    void adminPersonaProvisionsMerchantStoreTerminalAndReadsBackConfiguration() throws Exception {
        MvcResult merchantResult = mockMvc.perform(post("/api/identity/merchants")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "code": "MER-UAT-ADMIN",
                                  "name": "UAT Admin Merchant"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long merchantId = readId(merchantResult);

        MvcResult storeResult = mockMvc.perform(post("/api/identity/stores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "code": "STORE-UAT-ADMIN",
                                  "name": "UAT Admin Store"
                                }
                                """.formatted(merchantId)))
                .andExpect(status().isCreated())
                .andReturn();
        long storeId = readId(storeResult);

        MvcResult terminalResult = mockMvc.perform(post("/api/identity/terminals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "code": "TERM-UAT-ADMIN",
                                  "name": "UAT Admin Terminal"
                                }
                                """.formatted(storeId)))
                .andExpect(status().isCreated())
                .andReturn();
        long terminalId = readId(terminalResult);

        mockMvc.perform(get("/api/identity/terminals/{id}", terminalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.storeLocationId").value(storeId))
                .andExpect(jsonPath("$.code").value("TERM-UAT-ADMIN"))
                .andExpect(jsonPath("$.active").value(true));
    }

    private long openShift(Long cashierUserId, Long terminalDeviceId, String openingCash) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/shifts/open")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "openingCash": %s
                                }
                                """.formatted(cashierUserId, terminalDeviceId, openingCash)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private long createCart(Long cashierUserId, Long storeLocationId, Long terminalDeviceId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sales/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "storeLocationId": %d,
                                  "terminalDeviceId": %d,
                                  "pricingAt": "2026-02-10T08:00:00Z"
                                }
                                """.formatted(cashierUserId, storeLocationId, terminalDeviceId)))
                .andExpect(status().isCreated())
                .andReturn();
        return readId(result);
    }

    private void seedVoidReasonCodes() {
        jdbcTemplate.update(
                "INSERT INTO void_reason_code (merchant_id, code, description, is_active, created_at, updated_at) "
                        + "VALUES (NULL, ?, ?, TRUE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)",
                "PRICE_MATCH",
                "Price match");
    }

    private Long createUser(String username) {
        UserAccountEntity user = new UserAccountEntity();
        user.setUsername(username);
        user.setPasswordHash("hash");
        user.setActive(true);
        return userAccountRepository.save(user).getId();
    }

    private MerchantEntity createMerchant(String code, String name) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode(code);
        merchant.setName(name);
        merchant.setActive(true);
        return merchantRepository.save(merchant);
    }

    private StoreLocationEntity createStore(MerchantEntity merchant, String code, String name) {
        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode(code);
        store.setName(name);
        store.setActive(true);
        return storeLocationRepository.save(store);
    }

    private TerminalDeviceEntity createTerminal(StoreLocationEntity store, String code, String name) {
        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(store);
        terminal.setCode(code);
        terminal.setName(name);
        terminal.setActive(true);
        return terminalDeviceRepository.save(terminal);
    }

    private CategoryEntity createCategory(MerchantEntity merchant, String code, String name) {
        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode(code);
        category.setName(name);
        category.setActive(true);
        return categoryRepository.save(category);
    }

    private TaxGroupEntity createTaxGroup(MerchantEntity merchant, String code) {
        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode(code);
        taxGroup.setName(code);
        taxGroup.setTaxRatePercent(new BigDecimal("10.0000"));
        taxGroup.setZeroRated(false);
        taxGroup.setActive(true);
        return taxGroupRepository.save(taxGroup);
    }

    private void createStoreTaxRule(StoreLocationEntity store, TaxGroupEntity taxGroup) {
        StoreTaxRuleEntity taxRule = new StoreTaxRuleEntity();
        taxRule.setStoreLocation(store);
        taxRule.setTaxGroup(taxGroup);
        taxRule.setTaxMode(TaxMode.EXCLUSIVE);
        taxRule.setExempt(false);
        taxRule.setActive(true);
        storeTaxRuleRepository.save(taxRule);
    }

    private Long createProduct(MerchantEntity merchant,
                               StoreLocationEntity store,
                               String sku,
                               String name,
                               String basePrice) {
        CategoryEntity category = createCategory(merchant, "CAT-" + sku, "Category " + sku);
        TaxGroupEntity taxGroup = createTaxGroup(merchant, "VAT-" + sku);
        createStoreTaxRule(store, taxGroup);
        ProductEntity product = createSimpleProduct(merchant, category, taxGroup, sku, name, basePrice);
        return product.getId();
    }

    private ProductEntity createSimpleProduct(MerchantEntity merchant,
                                              CategoryEntity category,
                                              TaxGroupEntity taxGroup,
                                              String sku,
                                              String name,
                                              String basePrice) {
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

    private SupplierEntity createSupplier(MerchantEntity merchant, String code, String name) {
        SupplierEntity supplier = new SupplierEntity();
        supplier.setMerchant(merchant);
        supplier.setCode(code);
        supplier.setName(name);
        supplier.setActive(true);
        return supplierRepository.save(supplier);
    }

    private long readId(MvcResult result) throws Exception {
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.path("id").asLong();
    }
}
