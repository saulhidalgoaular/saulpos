package com.saulpos.server.inventory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.supplier.model.SupplierEntity;
import com.saulpos.server.supplier.repository.SupplierRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "inventory-supplier-return", authorities = {"PERM_INVENTORY_ADJUST"})
class SupplierReturnIntegrationTest {

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
    private SupplierRepository supplierRepository;

    private Long storeId;
    private Long supplierId;
    private Long productId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM supplier_return_line");
        jdbcTemplate.execute("DELETE FROM supplier_return");
        jdbcTemplate.execute("DELETE FROM inventory_product_cost");
        jdbcTemplate.execute("DELETE FROM inventory_movement_lot");
        jdbcTemplate.execute("DELETE FROM inventory_lot_balance");
        jdbcTemplate.execute("DELETE FROM inventory_lot");
        jdbcTemplate.execute("DELETE FROM goods_receipt");
        jdbcTemplate.execute("DELETE FROM purchase_order_line");
        jdbcTemplate.execute("DELETE FROM purchase_order");
        jdbcTemplate.execute("DELETE FROM stock_transfer_line");
        jdbcTemplate.execute("DELETE FROM stock_transfer");
        jdbcTemplate.execute("DELETE FROM stocktake_line");
        jdbcTemplate.execute("DELETE FROM stocktake_session");
        jdbcTemplate.execute("DELETE FROM stock_adjustment");
        jdbcTemplate.execute("DELETE FROM supplier_contact");
        jdbcTemplate.execute("DELETE FROM supplier_terms");
        jdbcTemplate.execute("DELETE FROM supplier");
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
        jdbcTemplate.execute("DELETE FROM loyalty_event");
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

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-I4");
        merchant.setName("Merchant I4");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity store = new StoreLocationEntity();
        store.setMerchant(merchant);
        store.setCode("STORE-I4");
        store.setName("Store I4");
        store.setActive(true);
        store = storeLocationRepository.save(store);
        storeId = store.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-I4");
        category.setName("Category I4");
        category.setActive(true);
        category = categoryRepository.save(category);

        ProductEntity product = new ProductEntity();
        product.setMerchant(merchant);
        product.setCategory(category);
        product.setSku("SKU-I4-001");
        product.setName("Product I4");
        product.setBasePrice(new BigDecimal("3.00"));
        product.setDescription("Supplier return product");
        product.setActive(true);
        product = productRepository.save(product);
        productId = product.getId();

        SupplierEntity supplier = new SupplierEntity();
        supplier.setMerchant(merchant);
        supplier.setCode("SUP-I4-001");
        supplier.setName("Supplier I4");
        supplier.setActive(true);
        supplier = supplierRepository.save(supplier);
        supplierId = supplier.getId();
    }

    @Test
    void supplierReturnLifecyclePostsOutboundInventoryMovements() throws Exception {
        createAndReceivePurchaseOrder("8.000");

        JsonNode created = createSupplierReturn("3.000", "2.2500");
        long supplierReturnId = created.get("id").asLong();

        mockMvc.perform(post("/api/inventory/supplier-returns/{supplierReturnId}/approve", supplierReturnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/api/inventory/supplier-returns/{supplierReturnId}/post", supplierReturnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "posted"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("POSTED"))
                .andExpect(jsonPath("$.postedBy").value("inventory-supplier-return"))
                .andExpect(jsonPath("$.lines[0].returnQuantity").value(3.000))
                .andExpect(jsonPath("$.lines[0].unitCost").value(2.2500))
                .andExpect(jsonPath("$.lines[0].lineTotal").value(6.7500))
                .andExpect(jsonPath("$.lines[0].inventoryMovementId").isNumber())
                .andExpect(jsonPath("$.totalCost").value(6.7500));

        mockMvc.perform(get("/api/inventory/balances")
                        .param("storeLocationId", String.valueOf(storeId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].quantityOnHand").value(5.000));

        MvcResult movementResult = mockMvc.perform(get("/api/inventory/movements")
                        .param("storeLocationId", String.valueOf(storeId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].referenceType").value("SUPPLIER_RETURN"))
                .andExpect(jsonPath("$[1].quantityDelta").value(-3.000))
                .andExpect(jsonPath("$[1].runningBalance").value(5.000))
                .andReturn();

        JsonNode movements = objectMapper.readTree(movementResult.getResponse().getContentAsString());
        assertThat(movements.get(1).get("referenceNumber").asText()).startsWith("SR-");
    }

    @Test
    void supplierReturnValidationsEnforceEligibilityAndLifecycle() throws Exception {
        createAndReceivePurchaseOrder("5.000");

        JsonNode created = createSupplierReturn("2.000", "1.9000");
        long supplierReturnId = created.get("id").asLong();

        mockMvc.perform(post("/api/inventory/supplier-returns/{supplierReturnId}/post", supplierReturnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/inventory/supplier-returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": %d,
                                  "storeLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "returnQuantity": 6.000,
                                      "unitCost": 2.0000
                                    }
                                  ]
                                }
                                """.formatted(supplierId, storeId, productId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/inventory/supplier-returns/{supplierReturnId}/approve", supplierReturnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/api/inventory/supplier-returns/{supplierReturnId}/approve", supplierReturnId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));
    }

    private void createAndReceivePurchaseOrder(String receivedQuantity) throws Exception {
        MvcResult createResult = mockMvc.perform(post("/api/inventory/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": %d,
                                  "storeLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "orderedQuantity": 8.000
                                    }
                                  ]
                                }
                                """.formatted(supplierId, storeId, productId)))
                .andExpect(status().isCreated())
                .andReturn();

        long purchaseOrderId = objectMapper.readTree(createResult.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/approve", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/receive", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "receivedQuantity": %s,
                                      "unitCost": 2.1000
                                    }
                                  ]
                                }
                                """.formatted(productId, receivedQuantity)))
                .andExpect(status().isOk());
    }

    private JsonNode createSupplierReturn(String quantity, String unitCost) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/inventory/supplier-returns")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": %d,
                                  "storeLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "returnQuantity": %s,
                                      "unitCost": %s
                                    }
                                  ],
                                  "note": "created"
                                }
                                """.formatted(supplierId, storeId, productId, quantity, unitCost)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.lines.length()").value(1))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
    }
}
