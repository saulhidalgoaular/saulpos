package com.saulpos.server.security.service;

import java.time.Instant;

public record SessionTokenBundle(
        Long userId,
        Long sessionId,
        String accessToken,
        String refreshToken,
        Instant accessTokenExpiresAt,
        Instant refreshTokenExpiresAt
) {
}
