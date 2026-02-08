package com.saulpos.api.security;

import java.util.Set;

public record RoleResponse(
        Long id,
        String code,
        String description,
        Set<String> permissionCodes
) {
}
