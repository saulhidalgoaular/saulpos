package com.saulpos.server.loyalty;

import com.saulpos.api.loyalty.LoyaltyOperationStatus;
import com.saulpos.server.customer.model.CustomerEntity;
import com.saulpos.server.customer.repository.CustomerRepository;
import com.saulpos.server.identity.model.MerchantEntity;
import com.saulpos.server.identity.model.StoreLocationEntity;
import com.saulpos.server.identity.repository.MerchantRepository;
import com.saulpos.server.identity.repository.StoreLocationRepository;
import com.saulpos.server.loyalty.service.LoyaltyProvider;
import com.saulpos.server.loyalty.service.LoyaltyProviderEarnCommand;
import com.saulpos.server.loyalty.service.LoyaltyProviderRedeemCommand;
import com.saulpos.server.loyalty.service.LoyaltyProviderResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.loyalty.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
class LoyaltyEnabledIntegrationTest {

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

    @MockBean
    private LoyaltyProvider loyaltyProvider;

    private StoreLocationEntity storeLocation;
    private CustomerEntity customer;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM loyalty_event");
        jdbcTemplate.execute("DELETE FROM customer_contact");
        jdbcTemplate.execute("DELETE FROM customer_tax_identity");
        jdbcTemplate.execute("DELETE FROM customer");
        jdbcTemplate.execute("DELETE FROM terminal_device");
        jdbcTemplate.execute("DELETE FROM store_location");
        jdbcTemplate.execute("DELETE FROM merchant");

        MerchantEntity merchant = new MerchantEntity();
        merchant.setCode("MER-LOY-EN");
        merchant.setName("Merchant Loyalty Enabled");
        merchant.setActive(true);
        merchant = merchantRepository.save(merchant);

        storeLocation = new StoreLocationEntity();
        storeLocation.setMerchant(merchant);
        storeLocation.setCode("STORE-LOY-EN");
        storeLocation.setName("Store Loyalty Enabled");
        storeLocation.setActive(true);
        storeLocation = storeLocationRepository.save(storeLocation);

        customer = new CustomerEntity();
        customer.setMerchant(merchant);
        customer.setDisplayName("Loyalty Customer Enabled");
        customer.setActive(true);
        customer = customerRepository.save(customer);

        when(loyaltyProvider.providerCode()).thenReturn("TEST_PROVIDER");
    }

    @Test
    void earnUsesProviderWhenEnabled() throws Exception {
        when(loyaltyProvider.earn(any(LoyaltyProviderEarnCommand.class)))
                .thenReturn(new LoyaltyProviderResult(
                        LoyaltyOperationStatus.APPLIED,
                        12,
                        "PROVIDER-EARN-REF-1",
                        "loyalty earn applied"));

        mockMvc.perform(post("/api/loyalty/earn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "customerId": %d,
                                  "reference": "SALE-LOY-EN-1",
                                  "saleGrossAmount": 49.90
                                }
                                """.formatted(storeLocation.getId(), customer.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationType").value("EARN"))
                .andExpect(jsonPath("$.status").value("APPLIED"))
                .andExpect(jsonPath("$.pointsDelta").value(12))
                .andExpect(jsonPath("$.providerCode").value("TEST_PROVIDER"));

        String savedStatus = jdbcTemplate.queryForObject("SELECT status FROM loyalty_event LIMIT 1", String.class);
        assertThat(savedStatus).isEqualTo("APPLIED");
    }

    @Test
    void redeemReturnsUnavailableWhenProviderThrows() throws Exception {
        when(loyaltyProvider.redeem(any(LoyaltyProviderRedeemCommand.class)))
                .thenThrow(new RuntimeException("provider connection failure"));

        mockMvc.perform(post("/api/loyalty/redeem")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "storeLocationId": %d,
                                  "customerId": %d,
                                  "reference": "SALE-LOY-EN-2",
                                  "requestedPoints": 25
                                }
                                """.formatted(storeLocation.getId(), customer.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operationType").value("REDEEM"))
                .andExpect(jsonPath("$.status").value("UNAVAILABLE"))
                .andExpect(jsonPath("$.pointsDelta").value(0))
                .andExpect(jsonPath("$.providerCode").value("TEST_PROVIDER"));

        String savedStatus = jdbcTemplate.queryForObject("SELECT status FROM loyalty_event LIMIT 1", String.class);
        assertThat(savedStatus).isEqualTo("UNAVAILABLE");
    }
}
