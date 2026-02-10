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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.Instant;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "inventory-operator", authorities = {"PERM_INVENTORY_ADJUST", "PERM_SALES_PROCESS"})
class InventoryLedgerIntegrationTest {

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

    private Long cashierUserId;
    private Long storeLocationId;
    private Long terminalDeviceId;
    private Long productId;

    @BeforeEach
    void setUp() {
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
        cashier.setUsername("cashier-user-h1");
        cashier.setPasswordHash("hash");
        cashier.setActive(true);
        cashier = userAccountRepository.save(cashier);
        cashierUserId = cashier.getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-H1");
        merchant.setName("Merchant H1");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-H1");
        storeLocation.setName("Store H1");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-H1");
        terminal.setName("Terminal H1");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);
        terminalDeviceId = terminal.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-H1");
        category.setName("Category H1");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10-H1");
        taxGroup.setName("VAT 10 H1");
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
        product.setSku("SKU-H1-001");
        product.setName("Ledger Product");
        product.setBasePrice(new BigDecimal("5.00"));
        product.setDescription("Inventory ledger test product");
        product.setActive(true);
        product = productRepository.save(product);
        productId = product.getId();
    }

    @Test
    void inventoryLedgerSupportsSaleReturnAndAdjustmentBalanceEffects() throws Exception {
        createManualMovement("ADJUSTMENT", "10.000", "STOCK_ADJUSTMENT", "ADJ-OPEN-001")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementType").value("ADJUSTMENT"))
                .andExpect(jsonPath("$.runningBalance").value(10.000));

        long cartId = createCart();
        addCartLine(cartId, "sale-line-1", 2);
        checkout(cartId);

        createManualMovement("RETURN", "1.000", "SALE_RETURN", "RET-001")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementType").value("RETURN"));

        createManualMovement("ADJUSTMENT", "-0.500", "STOCK_ADJUSTMENT", "ADJ-SHRINK-001")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.movementType").value("ADJUSTMENT"));

        mockMvc.perform(get("/api/inventory/movements")
                        .param("storeLocationId", String.valueOf(storeLocationId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].movementType").value("ADJUSTMENT"))
                .andExpect(jsonPath("$[0].referenceType").value("STOCK_ADJUSTMENT"))
                .andExpect(jsonPath("$[0].runningBalance").value(10.000))
                .andExpect(jsonPath("$[1].movementType").value("SALE"))
                .andExpect(jsonPath("$[1].runningBalance").value(8.000))
                .andExpect(jsonPath("$[2].movementType").value("RETURN"))
                .andExpect(jsonPath("$[2].runningBalance").value(9.000))
                .andExpect(jsonPath("$[3].movementType").value("ADJUSTMENT"))
                .andExpect(jsonPath("$[3].runningBalance").value(8.500));

        mockMvc.perform(get("/api/inventory/balances")
                        .param("storeLocationId", String.valueOf(storeLocationId))
                        .param("productId", String.valueOf(productId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].storeLocationId").value(storeLocationId))
                .andExpect(jsonPath("$[0].productId").value(productId))
                .andExpect(jsonPath("$[0].quantityOnHand").value(8.500));
    }

    @Test
    void manualMovementValidationRejectsUnsupportedTypeAndInvalidReference() throws Exception {
        createManualMovement("SALE", "1.000", "SALE_RECEIPT", "SALE-MANUAL")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        createManualMovement("RETURN", "1.000", "STOCK_ADJUSTMENT", "RET-INVALID")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        createManualMovement("RETURN", "-1.000", "SALE_RETURN", "RET-NEGATIVE")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    private org.springframework.test.web.servlet.ResultActions createManualMovement(String movementType,
                                                                                    String quantityDelta,
                                                                                    String referenceType,
                                                                                    String referenceNumber) throws Exception {
        return mockMvc.perform(post("/api/inventory/movements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "storeLocationId": %d,
                          "productId": %d,
                          "movementType": "%s",
                          "quantityDelta": %s,
                          "referenceType": "%s",
                          "referenceNumber": "%s"
                        }
                        """.formatted(storeLocationId, productId, movementType, quantityDelta, referenceType, referenceNumber)));
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

    private void addCartLine(long cartId, String lineKey, int quantity) throws Exception {
        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "%s",
                                  "productId": %d,
                                  "quantity": %d
                                }
                                """.formatted(lineKey, productId, quantity)))
                .andExpect(status().isOk());
    }

    private void checkout(long cartId) throws Exception {
        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "inventory-ledger-checkout-" + cartId)
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
                                """.formatted(cartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").isNumber());
    }
}
