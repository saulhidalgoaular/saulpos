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
class CustomerGroupIntegrationTest {

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
        jdbcTemplate.execute("DELETE FROM auth_audit_event");
        jdbcTemplate.execute("DELETE FROM auth_session");
        jdbcTemplate.execute("DELETE FROM user_role");
        jdbcTemplate.execute("DELETE FROM role_permission");
        jdbcTemplate.execute("DELETE FROM app_permission");
        jdbcTemplate.execute("DELETE FROM app_role");
        jdbcTemplate.execute("DELETE FROM user_account");

        merchantOne = createMerchant("MER-CGRP-01", "Merchant Group 01");
        merchantTwo = createMerchant("MER-CGRP-02", "Merchant Group 02");
    }

    @Test
    void createListAndAssignGroupsToCustomer() throws Exception {
        long customerId = createCustomer(merchantOne.getId(), "Group Buyer");
        long wholesaleGroupId = createCustomerGroup(merchantOne.getId(), "WHOLESALE", "Wholesale Group");
        long retailGroupId = createCustomerGroup(merchantOne.getId(), "RETAIL", "Retail Group");

        mockMvc.perform(get("/api/customers/groups")
                        .param("merchantId", String.valueOf(merchantOne.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("RETAIL"))
                .andExpect(jsonPath("$[1].code").value("WHOLESALE"));

        mockMvc.perform(put("/api/customers/{id}/groups", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerGroupIds": [%d, %d]
                                }
                                """.formatted(wholesaleGroupId, retailGroupId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerId))
                .andExpect(jsonPath("$.groups.length()").value(2))
                .andExpect(jsonPath("$.groups[0].code").value("RETAIL"))
                .andExpect(jsonPath("$.groups[1].code").value("WHOLESALE"));

        mockMvc.perform(get("/api/customers/{id}/groups", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].code").value("RETAIL"))
                .andExpect(jsonPath("$[1].code").value("WHOLESALE"));

        mockMvc.perform(put("/api/customers/{id}/groups", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerGroupIds": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups.length()").value(0));
    }

    @Test
    void assignGroupsRejectsCrossMerchantAssignments() throws Exception {
        long customerId = createCustomer(merchantOne.getId(), "Cross Merchant Buyer");
        long foreignGroupId = createCustomerGroup(merchantTwo.getId(), "FOREIGN", "Foreign Group");

        mockMvc.perform(put("/api/customers/{id}/groups", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerGroupIds": [%d]
                                }
                                """.formatted(foreignGroupId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }

    private long createCustomer(Long merchantId, String displayName) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "displayName": "%s"
                                }
                                """.formatted(merchantId, displayName)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private long createCustomerGroup(Long merchantId, String code, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/customers/groups")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "merchantId": %d,
                                  "code": "%s",
                                  "name": "%s"
                                }
                                """.formatted(merchantId, code, name)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
        return json.get("id").asLong();
    }

    private MerchantEntity createMerchant(String code, String name) {
        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode(code);
        merchant.setName(name);
        merchant.setActive(true);
        return merchantRepository.save(merchant);
    }
}
