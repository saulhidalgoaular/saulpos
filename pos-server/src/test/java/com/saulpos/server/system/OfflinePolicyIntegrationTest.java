package com.saulpos.server.system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OfflinePolicyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void offlinePolicyRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/system/offline-policy"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "policy-user", authorities = {"PERM_SALES_PROCESS"})
    void offlinePolicyReturnsSupportedModesAndMessages() throws Exception {
        mockMvc.perform(get("/api/system/offline-policy"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.policyVersion").value("K1-v1"))
                .andExpect(jsonPath("$.operations.length()").value(4))
                .andExpect(jsonPath("$.operations[0].operation").value("AUTH_LOGIN"))
                .andExpect(jsonPath("$.operations[0].mode").value("ONLINE_ONLY"))
                .andExpect(jsonPath("$.operations[3].operation").value("CATALOG_REFERENCE_VIEW"))
                .andExpect(jsonPath("$.operations[3].mode").value("DEGRADED_READ_ONLY"));
    }
}
