package com.saulpos.server.customer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class CustomerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MerchantRepository merchantRepository;

    private MerchantEntity merchantOne;
    private MerchantEntity merchantTwo;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
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
        jdbcTemplate.execute("DELETE FROM open_price_entry_audit");
        jdbcTemplate.execute("DELETE FROM store_price_override");
        jdbcTemplate.execute("DELETE FROM price_book_item");
        jdbcTemplate.execute("DELETE FROM price_book");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM inventory_movement");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM tax_group");
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

        merchantOne = createMerchant("MER-CUST-01", "Merchant Customer 01");
        merchantTwo = createMerchant("MER-CUST-02", "Merchant Customer 02");
    }

    @Test
    void createLookupAndUpdateCustomerFlowSupportsOptionalFields() throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d
                                }
                                """.formatted(merchantOne.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value(merchantOne.getId()))
                .andExpect(jsonPath("$.displayName").doesNotExist())
                .andExpect(jsonPath("$.invoiceRequired").value(false))
                .andExpect(jsonPath("$.creditEnabled").value(false))
                .andExpect(jsonPath("$.taxIdentities.length()").value(0))
                .andExpect(jsonPath("$.contacts.length()").value(0));

        MvcResult createdResult = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "displayName": "  Ana Perez  ",
                                  "invoiceRequired": true,
                                  "creditEnabled": true,
                                  "taxIdentities": [
                                    {
                                      "documentType": "nit",
                                      "documentValue": "123-ABC"
                                    }
                                  ],
                                  "contacts": [
                                    {
                                      "contactType": "EMAIL",
                                      "contactValue": "Ana.Perez@Example.com",
                                      "primary": true
                                    },
                                    {
                                      "contactType": "PHONE",
                                      "contactValue": "+593 099-123-4567",
                                      "primary": true
                                    }
                                  ]
                                }
                                """.formatted(merchantOne.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.displayName").value("Ana Perez"))
                .andExpect(jsonPath("$.invoiceRequired").value(true))
                .andExpect(jsonPath("$.creditEnabled").value(true))
                .andExpect(jsonPath("$.taxIdentities[0].documentType").value("NIT"))
                .andExpect(jsonPath("$.taxIdentities[0].documentValue").value("123-ABC"))
                .andExpect(jsonPath("$.contacts.length()").value(2))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createdResult.getResponse().getContentAsString());
        long customerId = createdJson.get("id").asLong();

        mockMvc.perform(get("/api/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.merchantId").value(merchantOne.getId()));

        mockMvc.perform(get("/api/customers/lookup")
                        .param("merchantId", String.valueOf(merchantOne.getId()))
                        .param("documentType", "nit")
                        .param("documentValue", "123-abc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(customerId));

        mockMvc.perform(get("/api/customers/lookup")
                        .param("merchantId", String.valueOf(merchantOne.getId()))
                        .param("email", "ANA.PEREZ@EXAMPLE.COM"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(customerId));

        mockMvc.perform(get("/api/customers/lookup")
                        .param("merchantId", String.valueOf(merchantOne.getId()))
                        .param("phone", "+5930991234567"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(customerId));

        mockMvc.perform(put("/api/customers/{id}", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "displayName": "Ana Maria Perez",
                                  "invoiceRequired": false,
                                  "creditEnabled": true,
                                  "taxIdentities": [
                                    {
                                      "documentType": "nit",
                                      "documentValue": "123-ABC"
                                    }
                                  ],
                                  "contacts": [
                                    {
                                      "contactType": "EMAIL",
                                      "contactValue": "ana.maria@example.com",
                                      "primary": true
                                    }
                                  ]
                                }
                                """.formatted(merchantOne.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Ana Maria Perez"))
                .andExpect(jsonPath("$.invoiceRequired").value(false))
                .andExpect(jsonPath("$.creditEnabled").value(true))
                .andExpect(jsonPath("$.contacts.length()").value(1));

        mockMvc.perform(post("/api/customers/{id}/deactivate", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/api/customers/{id}/activate", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void duplicateDocumentIsRejectedWithinMerchantAndAllowedAcrossMerchants() throws Exception {
        createCustomerWithDocument(merchantOne.getId(), "NIT", "DOC-777");

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "displayName": "Second Customer",
                                  "taxIdentities": [
                                    {
                                      "documentType": "nit",
                                      "documentValue": "doc-777"
                                    }
                                  ]
                                }
                                """.formatted(merchantOne.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "displayName": "Third Customer",
                                  "taxIdentities": [
                                    {
                                      "documentType": "NIT",
                                      "documentValue": "DOC-777"
                                    }
                                  ]
                                }
                                """.formatted(merchantTwo.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value(merchantTwo.getId()));
    }

    @Test
    void lookupValidationRequiresExactlyOneCriterion() throws Exception {
        mockMvc.perform(get("/api/customers/lookup")
                        .param("merchantId", String.valueOf(merchantOne.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(get("/api/customers/lookup")
                        .param("merchantId", String.valueOf(merchantOne.getId()))
                        .param("email", "one@example.com")
                        .param("phone", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        mockMvc.perform(get("/api/customers/lookup")
                        .param("merchantId", String.valueOf(merchantOne.getId()))
                        .param("documentType", "NIT"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    private void createCustomerWithDocument(Long merchantId, String documentType, String documentValue) throws Exception {
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "displayName": "Base Customer",
                                  "taxIdentities": [
                                    {
                                      "documentType": "%s",
                                      "documentValue": "%s"
                                    }
                                  ]
                                }
                                """.formatted(merchantId, documentType, documentValue)))
                .andExpect(status().isCreated());
    }

    private MerchantEntity createMerchant(String code, String name) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode(code);
        merchant.setName(name);
        merchant.setActive(true);
        return merchantRepository.save(merchant);
    }
}
