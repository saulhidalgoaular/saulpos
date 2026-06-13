package com.saulpos.server.security.service;

import com.saulpos.api.security.CurrentUserPermissionsResponse;
import com.saulpos.api.security.PermissionResponse;
import com.saulpos.api.security.RolePermissionsUpdateRequest;
import com.saulpos.api.security.RoleRequest;
import com.saulpos.api.security.RoleResponse;
import com.saulpos.api.security.UserAccountCreateRequest;
import com.saulpos.api.security.UserAccountPasswordResetRequest;
import com.saulpos.api.security.UserAccountResponse;
import com.saulpos.server.error.BaseException;
import com.saulpos.server.error.ErrorCode;
import com.saulpos.server.security.authorization.SecurityAuthority;
import com.saulpos.server.security.model.PermissionEntity;
import com.saulpos.server.security.model.RoleEntity;
import com.saulpos.server.security.model.RolePermissionEntity;
import com.saulpos.server.security.model.UserAccountEntity;
import com.saulpos.server.security.repository.PermissionRepository;
import com.saulpos.server.security.repository.RolePermissionRepository;
import com.saulpos.server.security.repository.RoleRepository;
import com.saulpos.server.security.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SecurityManagementService {

    private final AuthContextService authContextService;
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final RolePermissionRepository rolePermissionRepository;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public CurrentUserPermissionsResponse currentUserPermissions() {
        AuthenticatedUser user = authContextService.requireCurrentUser();
        return new CurrentUserPermissionsResponse(
                user.userId(),
                user.username(),
                sortSet(user.roles()),
                sortSet(user.permissions()));
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissionCatalog() {
        return permissionRepository.findAllByOrderByCodeAsc().stream()
                .map(permission -> new PermissionResponse(
                        permission.getId(),
                        permission.getCode(),
                        permission.getDescription()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roleRepository.findAllByOrderByCodeAsc().stream()
                .map(this::toRoleResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserAccountResponse> listUsers() {
        return userAccountRepository.findAllByOrderByUsernameAsc().stream()
                .map(this::toUserAccountResponse)
                .toList();
    }

    @Transactional
    public UserAccountResponse createUser(UserAccountCreateRequest request) {
        String username = normalizeUsername(request.username());
        userAccountRepository.findByUsernameIgnoreCase(username).ifPresent(existing -> {
            throw usernameConflict(username);
        });

        UserAccountEntity user = new UserAccountEntity();
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setActive(true);
        user.setFailedAttempts(0);
        user.setLockedUntil(null);

        try {
            return toUserAccountResponse(userAccountRepository.save(user));
        } catch (DataIntegrityViolationException ex) {
            throw usernameConflict(username);
        }
    }

    @Transactional
    public UserAccountResponse setUserActive(Long userId, boolean active) {
        UserAccountEntity user = requireUser(userId);
        user.setActive(active);
        return toUserAccountResponse(userAccountRepository.save(user));
    }

    @Transactional
    public UserAccountResponse resetUserPassword(Long userId, UserAccountPasswordResetRequest request) {
        UserAccountEntity user = requireUser(userId);
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setFailedAttempts(0);
        user.setLockedUntil(null);
        return toUserAccountResponse(userAccountRepository.save(user));
    }

    @Transactional
    public RoleResponse createRole(RoleRequest request) {
        String code = normalizeCode(request.code());
        roleRepository.findByCode(code).ifPresent(existing -> {
            throw new BaseException(ErrorCode.CONFLICT, "role code already exists: " + code);
        });

        RoleEntity role = new RoleEntity();
        role.setCode(code);
        role.setDescription(normalizeDescription(request.description()));
        RoleEntity savedRole = roleRepository.save(role);

        upsertRolePermissions(savedRole, request.permissionCodes());
        return toRoleResponse(savedRole);
    }

    @Transactional
    public RoleResponse updateRolePermissions(Long roleId, RolePermissionsUpdateRequest request) {
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "role not found: " + roleId));

        upsertRolePermissions(role, request.permissionCodes());
        return toRoleResponse(role);
    }

    private RoleResponse toRoleResponse(RoleEntity role) {
        Set<String> permissionCodes = new TreeSet<>(rolePermissionRepository.findPermissionCodesByRoleId(role.getId()));
        return new RoleResponse(
                role.getId(),
                role.getCode(),
                role.getDescription(),
                permissionCodes);
    }

    private UserAccountResponse toUserAccountResponse(UserAccountEntity user) {
        return new UserAccountResponse(
                user.getId(),
                user.getUsername(),
                user.isActive(),
                user.getFailedAttempts(),
                user.getLockedUntil(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }

    private void upsertRolePermissions(RoleEntity role, Set<String> requestedPermissionCodes) {
        rolePermissionRepository.deleteByRoleId(role.getId());
        if (requestedPermissionCodes == null || requestedPermissionCodes.isEmpty()) {
            return;
        }

        Set<String> normalizedCodes = requestedPermissionCodes.stream()
                .map(this::normalizeCode)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        List<PermissionEntity> permissions = permissionRepository.findByCodeIn(normalizedCodes);

        Map<String, PermissionEntity> permissionByCode = permissions.stream()
                .collect(Collectors.toMap(PermissionEntity::getCode, Function.identity()));

        for (String code : normalizedCodes) {
            PermissionEntity permission = permissionByCode.get(code);
            if (permission == null) {
                throw new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "permission not found: " + code);
            }

            RolePermissionEntity rolePermission = new RolePermissionEntity();
            rolePermission.setRole(role);
            rolePermission.setPermission(permission);
            rolePermissionRepository.save(rolePermission);
        }
    }

    private UserAccountEntity requireUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new BaseException(ErrorCode.RESOURCE_NOT_FOUND, "user not found: " + userId));
    }

    private BaseException usernameConflict(String username) {
        return new BaseException(ErrorCode.CONFLICT, "username already exists: " + username);
    }

    private String normalizeCode(String code) {
        return SecurityAuthority.normalize(code);
    }

    private String normalizeUsername(String username) {
        return username.trim();
    }

    private String normalizeDescription(String description) {
        if (description == null) {
            return null;
        }
        String normalized = description.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Set<String> sortSet(Set<String> values) {
        return values.stream()
                .map(value -> value.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
