package com.saulpos.api.security;

import java.util.Set;

public record CurrentUserPermissionsResponse(
        Long userId,
        String username,
        Set<String> roles,
        Set<String> permissions
) {
}
