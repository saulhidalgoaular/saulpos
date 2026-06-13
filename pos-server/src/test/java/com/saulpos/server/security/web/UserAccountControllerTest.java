package com.saulpos.server.security.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saulpos.api.security.UserAccountCreateRequest;
import com.saulpos.api.security.UserAccountResponse;
import com.saulpos.server.error.GlobalExceptionHandler;
import com.saulpos.server.security.service.SecurityManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserAccountControllerTest {

    private SecurityManagementService securityManagementService;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        securityManagementService = mock(SecurityManagementService.class);
        UserAccountController controller = new UserAccountController(securityManagementService);
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void listShouldReturnCatalog() throws Exception {
        when(securityManagementService.listUsers()).thenReturn(List.of(user(5L, "cashier", true)));

        mockMvc.perform(get("/api/security/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(5L))
                .andExpect(jsonPath("$[0].username").value("cashier"));
    }

    @Test
    void createShouldReturnCreatedAndDelegateToService() throws Exception {
        when(securityManagementService.createUser(any())).thenReturn(user(7L, "manager", true));

        mockMvc.perform(post("/api/security/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UserAccountCreateRequest("manager", "Pass!123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7L))
                .andExpect(jsonPath("$.username").value("manager"));

        verify(securityManagementService).createUser(any());
    }

    @Test
    void activateAndDeactivateShouldDelegateToService() throws Exception {
        when(securityManagementService.setUserActive(9L, true)).thenReturn(user(9L, "cashier9", true));
        when(securityManagementService.setUserActive(9L, false)).thenReturn(user(9L, "cashier9", false));

        mockMvc.perform(post("/api/security/users/{id}/activate", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true));

        mockMvc.perform(post("/api/security/users/{id}/deactivate", 9L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        verify(securityManagementService).setUserActive(9L, true);
        verify(securityManagementService).setUserActive(9L, false);
    }

    @Test
    void resetPasswordShouldValidateRequestBody() throws Exception {
        mockMvc.perform(post("/api/security/users/{id}/password-reset", 11L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("POS-4001"));

        verify(securityManagementService, org.mockito.Mockito.never()).resetUserPassword(eq(11L), any());
    }

    private static UserAccountResponse user(Long id, String username, boolean active) {
        Instant now = Instant.parse("2026-02-10T12:00:00Z");
        return new UserAccountResponse(id, username, active, 0, null, now, now);
    }
}
