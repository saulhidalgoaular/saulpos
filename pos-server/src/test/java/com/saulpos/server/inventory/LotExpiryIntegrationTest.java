package com.saulpos.server.inventory;

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
import com.saulpos.server.inventory.service.InventoryBalanceCalculator;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.inventory.expiry-override-enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LotExpiryIntegrationTest {

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
    private SupplierRepository supplierRepository;

    @Autowired
    private TaxGroupRepository taxGroupRepository;

    @Autowired
    private StoreTaxRuleRepository storeTaxRuleRepository;

    @Autowired
    private InventoryBalanceCalculator balanceCalculator;

    private Long cashierUserId;
    private Long storeLocationId;
    private Long terminalDeviceId;
    private Long productId;
    private Long supplierId;

    @BeforeEach
    void setUp() {
        for (String sql : List.of(
                "DELETE FROM inventory_movement_lot",
                "DELETE FROM inventory_lot_balance",
                "DELETE FROM inventory_lot",
                "DELETE FROM goods_receipt",
                "DELETE FROM purchase_order_line",
                "DELETE FROM purchase_order",
                "DELETE FROM stock_transfer_line",
                "DELETE FROM stock_transfer",
                "DELETE FROM stocktake_line",
                "DELETE FROM stocktake_session",
                "DELETE FROM stock_adjustment",
                "DELETE FROM supplier_contact",
                "DELETE FROM supplier_terms",
                "DELETE FROM supplier",
                "DELETE FROM sale_return_refund",
                "DELETE FROM sale_return_line",
                "DELETE FROM sale_return",
                "DELETE FROM payment_transition",
                "DELETE FROM payment_allocation",
                "DELETE FROM payment",
                "DELETE FROM inventory_movement",
                "DELETE FROM sale_line",
                "DELETE FROM sale",
                "DELETE FROM sale_override_event",
                "DELETE FROM sale_cart_event",
                "DELETE FROM parked_cart_reference",
                "DELETE FROM sale_cart_line",
                "DELETE FROM sale_cart",
                "DELETE FROM customer_group_assignment",
                "DELETE FROM customer_group",
                "DELETE FROM customer_contact",
                "DELETE FROM customer_tax_identity",
                "DELETE FROM customer",
                "DELETE FROM loyalty_event",
                "DELETE FROM promotion_window",
                "DELETE FROM promotion_rule",
                "DELETE FROM promotion",
                "DELETE FROM discount_application",
                "DELETE FROM discount_reason_code",
                "DELETE FROM receipt_header",
                "DELETE FROM receipt_sequence",
                "DELETE FROM receipt_series",
                "DELETE FROM rounding_policy",
                "DELETE FROM store_tax_rule",
                "DELETE FROM tax_group",
                "DELETE FROM open_price_entry_audit",
                "DELETE FROM store_price_override",
                "DELETE FROM price_book_item",
                "DELETE FROM price_book",
                "DELETE FROM product_barcode",
                "DELETE FROM product_variant",
                "DELETE FROM product",
                "DELETE FROM category",
                "DELETE FROM cash_movement",
                "DELETE FROM cash_shift",
                "DELETE FROM store_user_assignment",
                "DELETE FROM terminal_device",
                "DELETE FROM store_location",
                "DELETE FROM merchant",
                "DELETE FROM auth_audit_event",
                "DELETE FROM auth_session",
                "DELETE FROM user_role",
                "DELETE FROM role_permission",
                "DELETE FROM app_permission",
                "DELETE FROM app_role",
                "DELETE FROM user_account"
        )) {
            jdbcTemplate.execute(sql);
        }

        UserAccountEntity cashier = new UserAccountEntity();
        cashier.setUsername("lot-cashier");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashier = userAccountRepository.save(cashier);
        cashierUserId = cashier.getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-H5");
        merchant.setName("Merchant H5");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-H5");
        storeLocation.setName("Store H5");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-H5");
        terminal.setName("Terminal H5");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);
        terminalDeviceId = terminal.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-H5");
        category.setName("Category H5");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10-H5");
        taxGroup.setName("VAT 10 H5");
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
        product.setSku("SKU-H5-001");
        product.setName("Lot Tracked Product");
        product.setBasePrice(new BigDecimal("10.00"));
        product.setLotTrackingEnabled(true);
        product.setDescription("Lot + expiry tracked");
        product.setActive(true);
        product = productRepository.save(product);
        productId = product.getId();

        SupplierEntity supplier = new SupplierEntity();
        supplier.setMerchant(merchant);
        supplier.setCode("SUP-H5-001");
        supplier.setName("Supplier H5");
        supplier.setActive(true);
        supplier = supplierRepository.save(supplier);
        supplierId = supplier.getId();
    }

    @Test
    @WithMockUser(username = "lot-operator", authorities = {"PERM_INVENTORY_ADJUST", "PERM_SALES_PROCESS"})
    void receivesLotsUsesFefoAndBlocksExpiredStockWithoutManagerOverride() throws Exception {
        receiveLotTrackedStock(new BigDecimal("3.000"), LocalDate.now().minusDays(1),
                new BigDecimal("2.000"), LocalDate.now().plusDays(5));

        long firstCartId = createCart();
        addLine(firstCartId, new BigDecimal("2.000"));
        checkout(firstCartId, new BigDecimal("22.00"));

        mockMvc.perform(get("/api/inventory/movements")
                        .param("storeLocationId", String.valueOf(storeLocationId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].movementType").value("SALE"))
                .andExpect(jsonPath("$[1].lots.length()").value(1))
                .andExpect(jsonPath("$[1].lots[0].expiryState").value("ACTIVE"))
                .andExpect(jsonPath("$[1].lots[0].quantity").value(2.000));

        long secondCartId = createCart();
        addLine(secondCartId, new BigDecimal("1.000"));

        mockMvc.perform(post("/api/sales/checkout")
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
                                """.formatted(secondCartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(get("/api/inventory/balances")
                        .param("storeLocationId", String.valueOf(storeLocationId))
                        .param("productId", String.valueOf(productId))
                        .param("lotLevel", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].lotCode").value("LOT-EXP"))
                .andExpect(jsonPath("$[0].expiryState").value("EXPIRED"))
                .andExpect(jsonPath("$[0].quantityOnHand").value(3.000));
    }

    @Test
    @WithMockUser(username = "lot-manager", authorities = {
            "PERM_INVENTORY_ADJUST",
            "PERM_SALES_PROCESS",
            "PERM_CONFIGURATION_MANAGE"
    })
    void managerCanSellExpiredStockWhenOverridePolicyIsEnabled() throws Exception {
        receiveLotTrackedStock(new BigDecimal("2.000"), LocalDate.now().minusDays(2),
                null, null);

        long cartId = createCart();
        addLine(cartId, new BigDecimal("1.000"));
        checkout(cartId, new BigDecimal("11.00"));

        mockMvc.perform(get("/api/inventory/movements")
                        .param("storeLocationId", String.valueOf(storeLocationId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].movementType").value("SALE"))
                .andExpect(jsonPath("$[1].lots[0].expiryState").value("EXPIRED"))
                .andExpect(jsonPath("$[1].lots[0].quantity").value(1.000));

        BigDecimal remaining = jdbcTemplate.queryForObject(
                "SELECT quantity_on_hand FROM inventory_lot_balance b JOIN inventory_lot l ON l.id = b.inventory_lot_id WHERE l.product_id = ?",
                BigDecimal.class,
                productId);
        assertThat(balanceCalculator.normalizeScale(remaining)).isEqualByComparingTo(new BigDecimal("1.000"));
    }

    private void receiveLotTrackedStock(BigDecimal expiredQty,
                                        LocalDate expiredDate,
                                        BigDecimal activeQty,
                                        LocalDate activeDate) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/inventory/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": %d,
                                  "storeLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "orderedQuantity": %s
                                    }
                                  ]
                                }
                                """.formatted(
                                supplierId,
                                storeLocationId,
                                productId,
                                activeQty == null ? expiredQty.toPlainString() : expiredQty.add(activeQty).toPlainString())))
                .andExpect(status().isCreated())
                .andReturn();

        long purchaseOrderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/approve", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        StringBuilder lotsJson = new StringBuilder();
        lotsJson.append("""
                    {
                      "lotCode": "LOT-EXP",
                      "expiryDate": "%s",
                      "quantity": %s
                    }
                """.formatted(expiredDate, expiredQty.toPlainString()));
        if (activeQty != null) {
            lotsJson.append(",");
            lotsJson.append("""
                        {
                          "lotCode": "LOT-ACT",
                          "expiryDate": "%s",
                          "quantity": %s
                        }
                    """.formatted(activeDate, activeQty.toPlainString()));
        }

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/receive", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "receivedQuantity": %s,
                                      "lots": [
                                        %s
                                      ]
                                    }
                                  ]
                                }
                                """.formatted(
                                productId,
                                activeQty == null ? expiredQty.toPlainString() : expiredQty.add(activeQty).toPlainString(),
                                lotsJson)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"));
    }

    private long createCart() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/sales/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "storeLocationId": %d,
                                  "terminalDeviceId": %d,
                                  "pricingAt": "%s"
                                }
                                """.formatted(cashierUserId, storeLocationId, terminalDeviceId, Instant.now().toString())))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private void addLine(long cartId, BigDecimal quantity) throws Exception {
        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "lot-line-%d",
                                  "productId": %d,
                                  "quantity": %s
                                }
                                """.formatted(cartId, productId, quantity.toPlainString())))
                .andExpect(status().isOk());
    }

    private void checkout(long cartId, BigDecimal amount) throws Exception {
        mockMvc.perform(post("/api/sales/checkout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "payments": [
                                    {
                                      "tenderType": "CASH",
                                      "amount": %s,
                                      "tenderedAmount": %s
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId, amount.toPlainString(), amount.toPlainString())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").isNumber());
    }
}
