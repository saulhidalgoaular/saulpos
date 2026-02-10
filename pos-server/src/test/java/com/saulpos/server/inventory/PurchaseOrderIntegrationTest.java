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
@WithMockUser(username = "inventory-purchasing", authorities = {"PERM_INVENTORY_ADJUST"})
class PurchaseOrderIntegrationTest {

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
    private Long productOneId;
    private Long productTwoId;
    private Long otherMerchantSupplierId;

    @BeforeEach
    void setUp() {
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

        MerchantEntity merchant = createMerchant("MER-I2-01", "Merchant I2");
        StoreLocationEntity store = createStore(merchant, "STORE-I2-01", "I2 Main Store");
        storeId = store.getId();

        CategoryEntity category = createCategory(merchant, "CAT-I2", "Purchase Category");

        ProductEntity productOne = createProduct(merchant, category, "SKU-I2-001", "PO Product One", "2.50");
        ProductEntity productTwo = createProduct(merchant, category, "SKU-I2-002", "PO Product Two", "5.00");
        productOneId = productOne.getId();
        productTwoId = productTwo.getId();

        SupplierEntity supplier = createSupplier(merchant, "SUP-I2-001", "Supplier I2");
        supplierId = supplier.getId();

        MerchantEntity otherMerchant = createMerchant("MER-I2-02", "Merchant I2 Other");
        createStore(otherMerchant, "STORE-I2-02", "I2 Other Store");
        SupplierEntity otherSupplier = createSupplier(otherMerchant, "SUP-I2-002", "Supplier I2 Other");
        otherMerchantSupplierId = otherSupplier.getId();
    }

    @Test
    void purchaseOrderSupportsApprovePartialReceiveAndFullReceiveWithInventoryMovements() throws Exception {
        JsonNode created = createPurchaseOrder(supplierId, storeId, productOneId, productTwoId, "10.000", "5.000");
        long purchaseOrderId = created.get("id").asLong();

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/approve", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "note": "approved"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"))
                .andExpect(jsonPath("$.approvedBy").value("inventory-purchasing"));

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/receive", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "receivedQuantity": 4.000
                                    },
                                    {
                                      "productId": %d,
                                      "receivedQuantity": 5.000
                                    }
                                  ],
                                  "note": "first-receive"
                                }
                                """.formatted(productOneId, productTwoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_RECEIVED"))
                .andExpect(jsonPath("$.lines[0].receivedQuantity").value(4.000))
                .andExpect(jsonPath("$.lines[0].remainingQuantity").value(6.000))
                .andExpect(jsonPath("$.lines[1].receivedQuantity").value(5.000))
                .andExpect(jsonPath("$.lines[1].remainingQuantity").value(0.000))
                .andExpect(jsonPath("$.receipts.length()").value(1));

        mockMvc.perform(get("/api/inventory/balances")
                        .param("storeLocationId", String.valueOf(storeId))
                        .param("productId", String.valueOf(productOneId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantityOnHand").value(4.000));

        mockMvc.perform(get("/api/inventory/balances")
                        .param("storeLocationId", String.valueOf(storeId))
                        .param("productId", String.valueOf(productTwoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantityOnHand").value(5.000));

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/receive", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "receivedQuantity": 6.000
                                    }
                                  ],
                                  "note": "final-receive"
                                }
                                """.formatted(productOneId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.lines[0].receivedQuantity").value(10.000))
                .andExpect(jsonPath("$.lines[0].remainingQuantity").value(0.000))
                .andExpect(jsonPath("$.receipts.length()").value(2));

        mockMvc.perform(get("/api/inventory/purchase-orders/{purchaseOrderId}", purchaseOrderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECEIVED"))
                .andExpect(jsonPath("$.lastReceivedBy").value("inventory-purchasing"))
                .andExpect(jsonPath("$.lines[1].receivedQuantity").value(5.000));

        MvcResult movementResult = mockMvc.perform(get("/api/inventory/movements")
                        .param("storeLocationId", String.valueOf(storeId))
                        .param("productId", String.valueOf(productOneId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].referenceType").value("PURCHASE_RECEIPT"))
                .andExpect(jsonPath("$[0].quantityDelta").value(4.000))
                .andExpect(jsonPath("$[1].referenceType").value("PURCHASE_RECEIPT"))
                .andExpect(jsonPath("$[1].quantityDelta").value(6.000))
                .andExpect(jsonPath("$[1].runningBalance").value(10.000))
                .andReturn();

        JsonNode movementJson = objectMapper.readTree(movementResult.getResponse().getContentAsString());
        assertThat(movementJson.get(0).get("referenceNumber").asText()).startsWith("GR-");
        assertThat(movementJson.get(1).get("referenceNumber").asText()).startsWith("GR-");
    }

    @Test
    void purchaseOrderValidationsEnforceMerchantConsistencyAndReceiveEligibility() throws Exception {
        mockMvc.perform(post("/api/inventory/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": %d,
                                  "storeLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "orderedQuantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(otherMerchantSupplierId, storeId, productOneId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        JsonNode created = createPurchaseOrder(supplierId, storeId, productOneId, productTwoId, "3.000", "2.000");
        long purchaseOrderId = created.get("id").asLong();

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/receive", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "receivedQuantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(productOneId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/approve", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/approve", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/receive", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "receivedQuantity": 4.000
                                    }
                                  ]
                                }
                                """.formatted(productOneId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/inventory/purchase-orders/{purchaseOrderId}/receive", purchaseOrderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": 999999,
                                      "receivedQuantity": 1.000
                                    }
                                  ]
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    private JsonNode createPurchaseOrder(Long supplierId,
                                         Long storeLocationId,
                                         Long firstProductId,
                                         Long secondProductId,
                                         String firstQuantity,
                                         String secondQuantity) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/inventory/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "supplierId": %d,
                                  "storeLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "orderedQuantity": %s
                                    },
                                    {
                                      "productId": %d,
                                      "orderedQuantity": %s
                                    }
                                  ],
                                  "note": "initial"
                                }
                                """.formatted(supplierId, storeLocationId, firstProductId, firstQuantity, secondProductId, secondQuantity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.supplierId").value(supplierId))
                .andExpect(jsonPath("$.storeLocationId").value(storeLocationId))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.lines.length()").value(2))
                .andExpect(jsonPath("$.lines[0].receivedQuantity").value(0.000))
                .andExpect(jsonPath("$.receipts.length()").value(0))
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString());
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

    private CategoryEntity createCategory(MerchantEntity merchant, String code, String name) {
        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode(code);
        category.setName(name);
        category.setActive(true);
        return categoryRepository.save(category);
    }

    private ProductEntity createProduct(MerchantEntity merchant,
                                        CategoryEntity category,
                                        String sku,
                                        String name,
                                        String basePrice) {
        ProductEntity product = new ProductEntity();
        product.setMerchant(merchant);
        product.setCategory(category);
        product.setSku(sku);
        product.setName(name);
        product.setBasePrice(new BigDecimal(basePrice));
        product.setDescription(name + " description");
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
}
