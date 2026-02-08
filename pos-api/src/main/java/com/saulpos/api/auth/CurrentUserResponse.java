package com.saulpos.api.auth;

import java.util.Set;

public record CurrentUserResponse(
        Long userId,
        String username,
        Set<String> roles
) {
}
