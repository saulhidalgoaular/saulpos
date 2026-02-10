package com.saulpos.client.state;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;

public record AuthSessionState(
        String username,
        String accessToken,
        String refreshToken,
        Set<String> permissions,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isBlank();
    }

    public boolean isAccessTokenExpired(Clock clock) {
        if (accessTokenExpiresAt == null) {
            return false;
        }
        return !accessTokenExpiresAt.isAfter(clock.instant());
    }
}
