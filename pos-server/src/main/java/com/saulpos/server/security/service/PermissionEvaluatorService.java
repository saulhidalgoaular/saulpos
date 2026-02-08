package com.saulpos.server.security.service;

import com.saulpos.server.security.authorization.SecurityAuthority;
import org.springframework.stereotype.Service;

@Service
public class PermissionEvaluatorService {

    private final AuthContextService authContextService;

    public PermissionEvaluatorService(AuthContextService authContextService) {
        this.authContextService = authContextService;
    }

    public boolean currentUserHasPermission(String permissionCode) {
        return hasPermission(authContextService.requireCurrentUser(), permissionCode);
    }

    public boolean hasPermission(AuthenticatedUser user, String permissionCode) {
        String normalizedPermission = SecurityAuthority.normalize(permissionCode);
        return user.permissions().stream()
                .map(SecurityAuthority::normalize)
                .anyMatch(normalizedPermission::equals);
    }
}
