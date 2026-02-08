package com.saulpos.server.security;

import com.saulpos.server.security.service.AuthContextService;
import com.saulpos.server.security.service.AuthenticatedUser;
import com.saulpos.server.security.service.PermissionEvaluatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class PermissionEvaluatorServiceTest {

    private AuthContextService authContextService;
    private PermissionEvaluatorService permissionEvaluatorService;

    @BeforeEach
    void setUp() {
        authContextService = Mockito.mock(AuthContextService.class);
        permissionEvaluatorService = new PermissionEvaluatorService(authContextService);
    }

    @Test
    void hasPermissionIsCaseInsensitiveAndReturnsTrueWhenPermissionExists() {
        AuthenticatedUser user = new AuthenticatedUser(
                1L,
                10L,
                "user",
                Set.of("MANAGER"),
                Set.of("SALES_PROCESS"));

        boolean hasPermission = permissionEvaluatorService.hasPermission(user, "sales_process");

        assertThat(hasPermission).isTrue();
    }

    @Test
    void currentUserHasPermissionReturnsFalseWhenPermissionIsMissing() {
        AuthenticatedUser user = new AuthenticatedUser(
                2L,
                20L,
                "user2",
                Set.of("CASHIER"),
                Set.of("SALES_PROCESS"));
        when(authContextService.requireCurrentUser()).thenReturn(user);

        boolean hasPermission = permissionEvaluatorService.currentUserHasPermission("REPORT_VIEW");

        assertThat(hasPermission).isFalse();
    }
}
