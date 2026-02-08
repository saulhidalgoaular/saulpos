package com.saulpos.server.security.service;

import java.util.Set;

public record AuthenticatedUser(
        Long userId,
        Long sessionId,
        String username,
        Set<String> roles
) {
}
