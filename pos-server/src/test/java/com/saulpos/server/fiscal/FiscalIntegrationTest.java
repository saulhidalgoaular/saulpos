package com.saulpos.server.fiscal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.api.tax.TaxMode;
import com.saulpos.server.catalog.model.CategoryEntity;
import com.saulpos.server.catalog.model.ProductEntity;
import com.saulpos.server.catalog.repository.CategoryRepository;
import com.saulpos.server.catalog.repository.ProductRepository;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.model.CustomerTaxIdentityEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class FiscalIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

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

    private Long cashierUserId;
    private Long storeLocationId;
    private Long terminalDeviceId;
    private Long unitProductId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM fiscal_event");
        jdbcTemplate.execute("DELETE FROM fiscal_document");
        jdbcTemplate.execute("DELETE FROM sale_return_refund");
        jdbcTemplate.execute("DELETE FROM sale_return_line");
        jdbcTemplate.execute("DELETE FROM sale_return");
        jdbcTemplate.execute("DELETE FROM payment_allocation");
        jdbcTemplate.execute("DELETE FROM payment_transition");
        jdbcTemplate.execute("DELETE FROM payment");
        jdbcTemplate.execute("DELETE FROM sale_line");
        jdbcTemplate.execute("DELETE FROM sale");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
        jdbcTemplate.execute("DELETE FROM receipt_header");
        jdbcTemplate.execute("DELETE FROM receipt_sequence");
        jdbcTemplate.execute("DELETE FROM receipt_series");
        jdbcTemplate.execute("DELETE FROM customer_group_assignment");
        jdbcTemplate.execute("DELETE FROM customer_group");
        jdbcTemplate.execute("DELETE FROM customer_contact");
        jdbcTemplate.execute("DELETE FROM customer_tax_identity");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM store_tax_rule");
        jdbcTemplate.execute("DELETE FROM tax_group");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM category");
        jdbcTemplate.execute("DELETE FROM terminal_device");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");
        jdbcTemplate.execute("DELETE FROM user_account");

        UserAccountEntity cashier = new UserAccountEntity();
        cashier.setUsername("fiscal-cashier");
        cashier.setPasswordHash(passwordEncoder.encode("Pass123!"));
        cashier.setActive(true);
        cashier = userAccountRepository.save(cashier);
        cashierUserId = cashier.getId();

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-FISCAL");
        merchant.setName("Merchant Fiscal");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        StoreLocationEntity storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-FISCAL");
        storeLocation.setName("Store Fiscal");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);
        storeLocationId = storeLocation.getId();

        TerminalDeviceEntity terminal = new TerminalDeviceEntity();
        terminal.setStoreLocation(storeLocation);
        terminal.setCode("TERM-FISCAL");
        terminal.setName("Terminal Fiscal");
        terminal.setActive(true);
        terminal = terminalDeviceRepository.save(terminal);
        terminalDeviceId = terminal.getId();

        CategoryEntity category = new CategoryEntity();
        category.setMerchant(merchant);
        category.setCode("CAT-FISCAL");
        category.setName("Category Fiscal");
        category.setActive(true);
        category = categoryRepository.save(category);

        TaxGroupEntity taxGroup = new TaxGroupEntity();
        taxGroup.setMerchant(merchant);
        taxGroup.setCode("VAT10");
        taxGroup.setName("VAT 10");
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
        product.setSku("SKU-FISCAL-001");
        product.setName("Product Fiscal");
        product.setBasePrice(new BigDecimal("6.00"));
        product.setActive(true);
        product = productRepository.save(product);
        unitProductId = product.getId();
    }

    @Test
    void checkoutRejectsInvoiceRequiredWhenCustomerIsMissing() throws Exception {
        long cartId = createCartWithOneLine();

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "fiscal-checkout-no-customer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "invoiceRequired": true,
                                  "payments": [
                                    {
                                      "sequenceNumber": 1,
                                      "tenderType": "CASH",
                                      "amount": 6.60,
                                      "tenderedAmount": 7.00,
                                      "reference": "cash"
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"))
                .andExpect(jsonPath("$.detail").value("customerId is required when invoiceRequired is true"));
    }

    @Test
    void checkoutWithInvoiceRequiredPersistsSkippedFiscalDocumentWhenProviderDisabled() throws Exception {
        long cartId = createCartWithOneLine();
        long customerId = createCustomerWithTaxIdentity();

        mockMvc.perform(post("/api/sales/checkout")
                        .header("Idempotency-Key", "fiscal-checkout-skipped")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cartId": %d,
                                  "cashierUserId": %d,
                                  "terminalDeviceId": %d,
                                  "customerId": %d,
                                  "invoiceRequired": true,
                                  "payments": [
                                    {
                                      "sequenceNumber": 1,
                                      "tenderType": "CASH",
                                      "amount": 6.60,
                                      "tenderedAmount": 7.00,
                                      "reference": "cash"
                                    }
                                  ]
                                }
                                """.formatted(cartId, cashierUserId, terminalDeviceId, customerId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").isNumber())
                .andExpect(jsonPath("$.receiptNumber").isString());

        Integer fiscalDocumentCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM fiscal_document WHERE status = 'SKIPPED' AND document_type = 'INVOICE'",
                Integer.class);
        Integer fiscalEventCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM fiscal_event WHERE event_type = 'ISSUE_SKIPPED'",
                Integer.class);

        assertThat(fiscalDocumentCount).isEqualTo(1);
        assertThat(fiscalEventCount).isEqualTo(1);
    }

    private long createCartWithOneLine() throws Exception {
        MvcResult cartResult = mockMvc.perform(post("/api/sales/carts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cashierUserId": %d,
                                  "storeLocationId": %d,
                                  "terminalDeviceId": %d,
                                  "pricingAt": "2026-02-10T10:00:00Z"
                                }
                                """.formatted(cashierUserId, storeLocationId, terminalDeviceId)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode cartJson = objectMapper.readTree(cartResult.getResponse().getContentAsString());
        long cartId = cartJson.get("id").asLong();

        mockMvc.perform(post("/api/sales/carts/{id}/lines", cartId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "lineKey": "fiscal-line-1",
                                  "productId": %d,
                                  "quantity": 1
                                }
                                """.formatted(unitProductId)))
                .andExpect(status().isOk());

        return cartId;
    }

    private long createCustomerWithTaxIdentity() {
        MerchantEntity merchant = merchantRepository.findAll().get(0);

        CustomerEntity customer = new CustomerEntity();
        customer.setMerchant(merchant);
        customer.setDisplayName("Fiscal Customer");
        customer.setInvoiceRequired(true);
        customer.setActive(true);

        CustomerTaxIdentityEntity taxIdentity = new CustomerTaxIdentityEntity();
        taxIdentity.setMerchant(merchant);
        taxIdentity.setDocumentType("RUC");
        taxIdentity.setDocumentValue("1234567890");
        taxIdentity.setActive(true);
        customer.addTaxIdentity(taxIdentity);

        return customerRepository.save(customer).getId();
    }
}
