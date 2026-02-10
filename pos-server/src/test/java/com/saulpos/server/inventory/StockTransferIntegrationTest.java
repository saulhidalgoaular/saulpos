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
@WithMockUser(username = "inventory-transfer", authorities = {"PERM_INVENTORY_ADJUST"})
class StockTransferIntegrationTest {

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

    private Long sourceStoreId;
    private Long destinationStoreId;
    private Long otherMerchantStoreId;
    private Long productOneId;
    private Long productTwoId;
    private Long externalProductId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM stock_transfer_line");
        jdbcTemplate.execute("DELETE FROM stock_transfer");
        jdbcTemplate.execute("DELETE FROM stocktake_line");
        jdbcTemplate.execute("DELETE FROM stocktake_session");
        jdbcTemplate.execute("DELETE FROM stock_adjustment");
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

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-H4");
        merchant.setName("Merchant H4");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity sourceStore = new StoreLocationEntity();
        sourceStore.setMerchant(merchant);
        sourceStore.setCode("STORE-H4-SRC");
        sourceStore.setName("H4 Source");
        sourceStore.setActive(true);
        sourceStore = storeLocationRepository.save(sourceStore);
        sourceStoreId = sourceStore.getId();

        StoreLocationEntity destinationStore = new StoreLocationEntity();
        destinationStore.setMerchant(merchant);
        destinationStore.setCode("STORE-H4-DST");
        destinationStore.setName("H4 Destination");
        destinationStore.setActive(true);
        destinationStore = storeLocationRepository.save(destinationStore);
        destinationStoreId = destinationStore.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-H4");
        category.setName("Transfer Category");
        category.setActive(true);
        category = categoryRepository.save(category);

        ProductEntity productOne = new ProductEntity();
        productOne.setMerchant(merchant);
        productOne.setCategory(category);
        productOne.setSku("SKU-H4-001");
        productOne.setName("Transfer Product One");
        productOne.setBasePrice(new BigDecimal("2.00"));
        productOne.setDescription("Transfer product one");
        productOne.setActive(true);
        productOne = productRepository.save(productOne);
        productOneId = productOne.getId();

        ProductEntity productTwo = new ProductEntity();
        productTwo.setMerchant(merchant);
        productTwo.setCategory(category);
        productTwo.setSku("SKU-H4-002");
        productTwo.setName("Transfer Product Two");
        productTwo.setBasePrice(new BigDecimal("3.00"));
        productTwo.setDescription("Transfer product two");
        productTwo.setActive(true);
        productTwo = productRepository.save(productTwo);
        productTwoId = productTwo.getId();

        MerchantEntity otherMerchant = new MerchantEntity();
        otherMerchant.setCode("MER-H4-EXT");
        otherMerchant.setName("Merchant H4 External");
        otherMerchant.setActive(true);
        otherMerchant = merchantRepository.save(otherMerchant);

        StoreLocationEntity otherStore = new StoreLocationEntity();
        otherStore.setMerchant(otherMerchant);
        otherStore.setCode("STORE-H4-EXT");
        otherStore.setName("H4 External");
        otherStore.setActive(true);
        otherStore = storeLocationRepository.save(otherStore);
        otherMerchantStoreId = otherStore.getId();

        CategoryEntity externalCategory = new CategoryEntity();
        externalCategory.setMerchant(otherMerchant);
        externalCategory.setCode("CAT-H4-EXT");
        externalCategory.setName("External Category");
        externalCategory.setActive(true);
        externalCategory = categoryRepository.save(externalCategory);

        ProductEntity externalProduct = new ProductEntity();
        externalProduct.setMerchant(otherMerchant);
        externalProduct.setCategory(externalCategory);
        externalProduct.setSku("SKU-H4-EXT-001");
        externalProduct.setName("External Product");
        externalProduct.setBasePrice(new BigDecimal("9.00"));
        externalProduct.setDescription("External product");
        externalProduct.setActive(true);
        externalProduct = productRepository.save(externalProduct);
        externalProductId = externalProduct.getId();
    }

    @Test
    void transferSupportsShipPartialReceiveAndFinalCompletionWithPairedMovements() throws Exception {
        createManualAdjustmentMovement(sourceStoreId, productOneId, "100.000", "ADJ-H4-P1");
        createManualAdjustmentMovement(sourceStoreId, productTwoId, "80.000", "ADJ-H4-P2");

        JsonNode createdTransfer = createTransfer(sourceStoreId, destinationStoreId, productOneId, productTwoId,
                "10.000", "7.000");
        long transferId = createdTransfer.get("id").asLong();
        String transferReference = createdTransfer.get("referenceNumber").asText();

        mockMvc.perform(post("/api/inventory/transfers/{transferId}/ship", transferId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "shippedQuantity": 10.000
                                    },
                                    {
                                      "productId": %d,
                                      "shippedQuantity": 7.000
                                    }
                                  ],
                                  "note": "shipment-confirmed"
                                }
                                """.formatted(productOneId, productTwoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"))
                .andExpect(jsonPath("$.lines[0].shippedQuantity").value(10.000))
                .andExpect(jsonPath("$.lines[0].receivedQuantity").value(0.000));

        String expectedOutboundRef = transferReference + "-P" + productOneId + "-OUT";
        mockMvc.perform(get("/api/inventory/movements")
                        .param("storeLocationId", String.valueOf(sourceStoreId))
                        .param("productId", String.valueOf(productOneId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].referenceType").value("STOCK_TRANSFER_OUT"))
                .andExpect(jsonPath("$[1].referenceNumber").value(expectedOutboundRef))
                .andExpect(jsonPath("$[1].quantityDelta").value(-10.000))
                .andExpect(jsonPath("$[1].runningBalance").value(90.000));

        mockMvc.perform(post("/api/inventory/transfers/{transferId}/receive", transferId)
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
                                      "receivedQuantity": 7.000
                                    }
                                  ],
                                  "note": "first-receive"
                                }
                                """.formatted(productOneId, productTwoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PARTIALLY_RECEIVED"))
                .andExpect(jsonPath("$.lines[0].receivedQuantity").value(4.000))
                .andExpect(jsonPath("$.lines[0].remainingQuantity").value(6.000))
                .andExpect(jsonPath("$.lines[1].receivedQuantity").value(7.000))
                .andExpect(jsonPath("$.lines[1].remainingQuantity").value(0.000));

        mockMvc.perform(post("/api/inventory/transfers/{transferId}/receive", transferId)
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
                .andExpect(jsonPath("$.lines[0].remainingQuantity").value(0.000));

        mockMvc.perform(get("/api/inventory/balances")
                        .param("storeLocationId", String.valueOf(destinationStoreId))
                        .param("productId", String.valueOf(productOneId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantityOnHand").value(10.000));

        mockMvc.perform(get("/api/inventory/balances")
                        .param("storeLocationId", String.valueOf(destinationStoreId))
                        .param("productId", String.valueOf(productTwoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].quantityOnHand").value(7.000));

        MvcResult destinationMovementResult = mockMvc.perform(get("/api/inventory/movements")
                        .param("storeLocationId", String.valueOf(destinationStoreId))
                        .param("productId", String.valueOf(productOneId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].referenceType").value("STOCK_TRANSFER_IN"))
                .andExpect(jsonPath("$[1].referenceType").value("STOCK_TRANSFER_IN"))
                .andReturn();

        JsonNode destinationMovements = objectMapper.readTree(destinationMovementResult.getResponse().getContentAsString());
        assertThat(destinationMovements.get(0).get("referenceNumber").asText())
                .startsWith(transferReference + "-P" + productOneId + "-IN-");
        assertThat(destinationMovements.get(1).get("referenceNumber").asText())
                .startsWith(transferReference + "-P" + productOneId + "-IN-");
    }

    @Test
    void transferValidationRejectsSameStoreDifferentMerchantAndOverReceive() throws Exception {
        mockMvc.perform(post("/api/inventory/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceStoreLocationId": %d,
                                  "destinationStoreLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "requestedQuantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(sourceStoreId, sourceStoreId, productOneId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(post("/api/inventory/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceStoreLocationId": %d,
                                  "destinationStoreLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "requestedQuantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(sourceStoreId, otherMerchantStoreId, productOneId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(post("/api/inventory/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceStoreLocationId": %d,
                                  "destinationStoreLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "requestedQuantity": 1.000
                                    }
                                  ]
                                }
                                """.formatted(sourceStoreId, destinationStoreId, externalProductId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        createManualAdjustmentMovement(sourceStoreId, productOneId, "50.000", "ADJ-H4-OVER");
        JsonNode createdTransfer = createTransfer(sourceStoreId, destinationStoreId, productOneId, productTwoId,
                "5.000", "2.000");
        long transferId = createdTransfer.get("id").asLong();

        mockMvc.perform(post("/api/inventory/transfers/{transferId}/ship", transferId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "shippedQuantity": 5.000
                                    },
                                    {
                                      "productId": %d,
                                      "shippedQuantity": 2.000
                                    }
                                  ]
                                }
                                """.formatted(productOneId, productTwoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));

        mockMvc.perform(post("/api/inventory/transfers/{transferId}/receive", transferId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "receivedQuantity": 6.000
                                    }
                                  ]
                                }
                                """.formatted(productOneId)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));
    }

    private JsonNode createTransfer(Long sourceId,
                                    Long destinationId,
                                    Long lineOneProductId,
                                    Long lineTwoProductId,
                                    String lineOneQuantity,
                                    String lineTwoQuantity) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/inventory/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "sourceStoreLocationId": %d,
                                  "destinationStoreLocationId": %d,
                                  "lines": [
                                    {
                                      "productId": %d,
                                      "requestedQuantity": %s
                                    },
                                    {
                                      "productId": %d,
                                      "requestedQuantity": %s
                                    }
                                  ],
                                  "note": "transfer-h4"
                                }
                                """.formatted(sourceId, destinationId, lineOneProductId, lineOneQuantity,
                                lineTwoProductId, lineTwoQuantity)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    private void createManualAdjustmentMovement(Long storeId,
                                                Long productId,
                                                String quantityDelta,
                                                String referenceNumber) throws Exception {
        mockMvc.perform(post("/api/inventory/movements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "productId": %d,
                                  "movementType": "ADJUSTMENT",
                                  "quantityDelta": %s,
                                  "referenceType": "STOCK_ADJUSTMENT",
                                  "referenceNumber": "%s"
                                }
                                """.formatted(storeId, productId, quantityDelta, referenceNumber)))
                .andExpect(status().isCreated());
    }
}
