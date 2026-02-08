package com.saulpos.api.auth;

import java.time.Instant;
import java.util.Set;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt,
        Set<String> roles
) {
}
