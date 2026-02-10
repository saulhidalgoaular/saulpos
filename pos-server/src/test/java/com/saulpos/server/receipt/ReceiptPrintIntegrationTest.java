package com.saulpos.server.receipt;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ReceiptPrintIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
    void printReturnsNotFoundWhenReceiptDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/receipts/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiptNumber": "RCPT-NOT-FOUND",
                                  "copy": false
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"PERM_CONFIGURATION_MANAGE"})
    void printRequiresSalesPermission() throws Exception {
        mockMvc.perform(post("/api/receipts/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiptNumber": "RCPT-ANY",
                                  "copy": true
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    @Test
    @WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
    void reprintRequiresReceiptReprintPermission() throws Exception {
        mockMvc.perform(post("/api/receipts/reprint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiptNumber": "RCPT-ANY"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POS-4030"));
    }

    @Test
    @WithMockUser(username = "manager", authorities = {"PERM_RECEIPT_REPRINT"})
    void reprintReturnsNotFoundWhenReceiptDoesNotExist() throws Exception {
        mockMvc.perform(post("/api/receipts/reprint")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiptNumber": "RCPT-NOT-FOUND"
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POS-4004"));
    }

    @Test
    @WithMockUser(username = "cashier", authorities = {"PERM_SALES_PROCESS"})
    void printValidatesRequestPayload() throws Exception {
        mockMvc.perform(post("/api/receipts/print")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "receiptNumber": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));
    }
}
