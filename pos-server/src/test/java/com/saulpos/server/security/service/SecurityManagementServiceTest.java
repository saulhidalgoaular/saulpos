package com.saulpos.server.security.service;

import com.saulpos.api.security.UserAccountCreateRequest;
import com.saulpos.api.security.UserAccountPasswordResetRequest;
import com.saulpos.api.security.UserAccountResponse;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.PermissionRepository;
import com.saulpos.server.security.repository.RolePermissionRepository;
import com.saulpos.server.security.repository.RoleRepository;
import com.saulpos.server.security.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityManagementServiceTest {

    @Mock
    private AuthContextService authContextService;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private RolePermissionRepository rolePermissionRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private SecurityManagementService securityManagementService;

    @Test
    void createUserShouldHashPasswordAndPersistNormalizedUsername() {
        when(userAccountRepository.findByUsernameIgnoreCase("cashier")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Pass!123")).thenReturn("bcrypt-hash");
        when(userAccountRepository.save(any(UserAccountEntity.class))).thenAnswer(invocation -> {
            UserAccountEntity entity = invocation.getArgument(0);
            entity.setId(15L);
            entity.setCreatedAt(Instant.parse("2026-02-10T12:00:00Z"));
            entity.setUpdatedAt(Instant.parse("2026-02-10T12:00:00Z"));
            return entity;
        });

        UserAccountResponse response = securityManagementService.createUser(
                new UserAccountCreateRequest("  cashier  ", "Pass!123"));

        ArgumentCaptor<UserAccountEntity> captor = ArgumentCaptor.forClass(UserAccountEntity.class);
        verify(userAccountRepository).save(captor.capture());
        UserAccountEntity saved = captor.getValue();

        assertEquals("cashier", saved.getUsername());
        assertEquals("bcrypt-hash", saved.getPasswordHash());
        assertEquals(15L, response.id());
        assertEquals("cashier", response.username());
        assertEquals(true, response.active());
    }

    @Test
    void createUserShouldRejectDuplicateUsername() {
        UserAccountEntity existing = new UserAccountEntity();
        existing.setId(1L);
        existing.setUsername("cashier");
        when(userAccountRepository.findByUsernameIgnoreCase("cashier")).thenReturn(Optional.of(existing));

        BaseException ex = assertThrows(BaseException.class,
                () -> securityManagementService.createUser(new UserAccountCreateRequest("cashier", "Pass!123")));

        assertEquals(ErrorCode.CONFLICT, ex.getErrorCode());
        assertEquals("username already exists: cashier", ex.getMessage());
    }

    @Test
    void setUserActiveShouldToggleFlag() {
        UserAccountEntity user = new UserAccountEntity();
        user.setId(7L);
        user.setUsername("operator");
        user.setActive(true);
        user.setFailedAttempts(0);
        user.setCreatedAt(Instant.parse("2026-02-10T12:00:00Z"));
        user.setUpdatedAt(Instant.parse("2026-02-10T12:00:00Z"));

        when(userAccountRepository.findById(7L)).thenReturn(Optional.of(user));
        when(userAccountRepository.save(any(UserAccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAccountResponse response = securityManagementService.setUserActive(7L, false);

        assertEquals(false, user.isActive());
        assertEquals(false, response.active());
    }

    @Test
    void resetUserPasswordShouldClearLockoutAndAttempts() {
        UserAccountEntity user = new UserAccountEntity();
        user.setId(8L);
        user.setUsername("operator");
        user.setPasswordHash("old-hash");
        user.setFailedAttempts(4);
        user.setLockedUntil(Instant.parse("2026-02-11T00:00:00Z"));
        user.setCreatedAt(Instant.parse("2026-02-10T12:00:00Z"));
        user.setUpdatedAt(Instant.parse("2026-02-10T12:00:00Z"));

        when(userAccountRepository.findById(8L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Reset!123")).thenReturn("new-hash");
        when(userAccountRepository.save(any(UserAccountEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAccountResponse response = securityManagementService.resetUserPassword(
                8L,
                new UserAccountPasswordResetRequest("Reset!123"));

        assertEquals("new-hash", user.getPasswordHash());
        assertEquals(0, user.getFailedAttempts());
        assertNull(user.getLockedUntil());
        assertEquals(0, response.failedAttempts());
    }
}
