package com.saulpos.api.security;

import java.time.Instant;

public record UserAccountResponse(
        Long id,
        String username,
        boolean active,
        int failedAttempts,
        Instant lockedUntil,
        Instant createdAt,
        Instant updatedAt
) {
}
