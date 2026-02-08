package com.saulpos.server.loyalty;

import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class LoyaltyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MerchantRepository merchantRepository;

    @Autowired
    private StoreLocationRepository storeLocationRepository;

    @Autowired
    private CustomerRepository customerRepository;

    private StoreLocationEntity storeLocation;
    private CustomerEntity customer;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM loyalty_event");
        jdbcTemplate.execute("DELETE FROM customer_contact");
        jdbcTemplate.execute("DELETE FROM customer_tax_identity");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-LOY-DIS");
        merchant.setName("Merchant Loyalty Disabled");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-LOY-DIS");
        storeLocation.setName("Store Loyalty Disabled");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);

        customer = new CustomerEntity();
        customer.setMerchant(merchant);
        customer.setDisplayName("Loyalty Customer Disabled");
        customer.setActive(true);
        customer = customerRepository.save(customer);
    }

    @Test
    void earnReturnsDisabledWhenIntegrationFlagIsOff() throws Exception {
        mockMvc.perform(post("/api/loyalty/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "customerId": %d,
                                  "reference": "SALE-LOY-DIS-1",
                                  "saleGrossAmount": 39.90
                                }
                                """.formatted(storeLocation.getId(), customer.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationType").value("EARN"))
                .andExpect(jsonPath("$.status").value("DISABLED"))
                .andExpect(jsonPath("$.pointsDelta").value(0))
                .andExpect(jsonPath("$.providerCode").value("STUB"));

        Integer eventCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM loyalty_event", Integer.class);
        String savedStatus = jdbcTemplate.queryForObject("SELECT status FROM loyalty_event LIMIT 1", String.class);
        assertThat(eventCount).isEqualTo(1);
        assertThat(savedStatus).isEqualTo("DISABLED");
    }
}
