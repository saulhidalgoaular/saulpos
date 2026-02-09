package com.saulpos.server.supplier;

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
@WithMockUser(username = "config-admin", authorities = {"PERM_CONFIGURATION_MANAGE"})
class SupplierIntegrationTest {

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
        jdbcTemplate.execute("DELETE FROM supplier_contact");
        jdbcTemplate.execute("DELETE FROM supplier_terms");
        jdbcTemplate.execute("DELETE FROM supplier");
        jdbcTemplate.execute("DELETE FROM customer_group_assignment");
        jdbcTemplate.execute("DELETE FROM customer_group");
        jdbcTemplate.execute("DELETE FROM customer_contact");
        jdbcTemplate.execute("DELETE FROM customer_tax_identity");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM sale_override_event");
        jdbcTemplate.execute("DELETE FROM sale_cart_event");
        jdbcTemplate.execute("DELETE FROM parked_cart_reference");
        jdbcTemplate.execute("DELETE FROM sale_cart_line");
        jdbcTemplate.execute("DELETE FROM sale_cart");
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
        jdbcTemplate.execute("DELETE FROM open_price_entry_audit");
        jdbcTemplate.execute("DELETE FROM store_price_override");
        jdbcTemplate.execute("DELETE FROM price_book_item");
        jdbcTemplate.execute("DELETE FROM price_book");
        jdbcTemplate.execute("DELETE FROM product_barcode");
        jdbcTemplate.execute("DELETE FROM product_variant");
        jdbcTemplate.execute("DELETE FROM product");
        jdbcTemplate.execute("DELETE FROM tax_group");
        jdbcTemplate.execute("DELETE FROM category");
        jdbcTemplate.execute("DELETE FROM cash_movement");
        jdbcTemplate.execute("DELETE FROM cash_shift");
        jdbcTemplate.execute("DELETE FROM store_user_assignment");
        jdbcTemplate.execute("DELETE FROM terminal_device");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");

        merchantOne = createMerchant("MER-SUP-01", "Supplier Merchant One");
        merchantTwo = createMerchant("MER-SUP-02", "Supplier Merchant Two");
    }

    @Test
    void createUpdateAndSearchSupplierFlowWorks() throws Exception {
        MvcResult createdResult = mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "code": "sup-001",
                                  "name": "Acme Distribution",
                                  "taxIdentifier": "RUC-001-999",
                                  "contacts": [
                                    {
                                      "contactType": "EMAIL",
                                      "contactValue": "sales@acme.test",
                                      "primary": true
                                    },
                                    {
                                      "contactType": "PHONE",
                                      "contactValue": "+593 099-111-2222",
                                      "primary": true
                                    }
                                  ],
                                  "terms": {
                                    "paymentTermDays": 30,
                                    "creditLimit": 1000.00,
                                    "notes": "Net 30"
                                  }
                                }
                                """.formatted(merchantOne.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value(merchantOne.getId()))
                .andExpect(jsonPath("$.code").value("SUP-001"))
                .andExpect(jsonPath("$.name").value("Acme Distribution"))
                .andExpect(jsonPath("$.taxIdentifier").value("RUC-001-999"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.contacts.length()").value(2))
                .andExpect(jsonPath("$.terms.paymentTermDays").value(30))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(createdResult.getResponse().getContentAsString());
        long supplierId = createdJson.get("id").asLong();

        mockMvc.perform(get("/api/suppliers/{id}", supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(supplierId))
                .andExpect(jsonPath("$.code").value("SUP-001"))
                .andExpect(jsonPath("$.contacts.length()").value(2));

        mockMvc.perform(get("/api/suppliers")
                        .param("merchantId", String.valueOf(merchantOne.getId()))
                        .param("q", "acme"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(supplierId));

        mockMvc.perform(put("/api/suppliers/{id}", supplierId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "code": "SUP-001",
                                  "name": "Acme Regional Distribution",
                                  "taxIdentifier": "RUC-001-999",
                                  "contacts": [
                                    {
                                      "contactType": "EMAIL",
                                      "contactValue": "ops@acme.test",
                                      "primary": true
                                    }
                                  ],
                                  "terms": {
                                    "paymentTermDays": 45,
                                    "creditLimit": 2000.00,
                                    "notes": "Net 45"
                                  }
                                }
                                """.formatted(merchantOne.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Regional Distribution"))
                .andExpect(jsonPath("$.contacts.length()").value(1))
                .andExpect(jsonPath("$.terms.paymentTermDays").value(45));

        mockMvc.perform(post("/api/suppliers/{id}/deactivate", supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/suppliers")
                        .param("merchantId", String.valueOf(merchantOne.getId()))
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(post("/api/suppliers/{id}/activate", supplierId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void uniqueIdentifiersAreEnforcedPerMerchant() throws Exception {
        createSupplier(merchantOne.getId(), "SUP-DUP", "RUC-777-ABC");

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "code": "sup-dup",
                                  "name": "Duplicate Code Supplier"
                                }
                                """.formatted(merchantOne.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "code": "SUP-OTHER",
                                  "name": "Duplicate Tax Supplier",
                                  "taxIdentifier": "ruc 777 abc"
                                }
                                """.formatted(merchantOne.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("POS-4009"));

        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "code": "SUP-DUP",
                                  "name": "Allowed Across Merchant",
                                  "taxIdentifier": "RUC-777-ABC"
                                }
                                """.formatted(merchantTwo.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.merchantId").value(merchantTwo.getId()));
    }

    private void createSupplier(Long merchantId, String code, String taxIdentifier) throws Exception {
        mockMvc.perform(post("/api/suppliers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "code": "%s",
                                  "name": "Seed Supplier",
                                  "taxIdentifier": "%s"
                                }
                                """.formatted(merchantId, code, taxIdentifier)))
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
