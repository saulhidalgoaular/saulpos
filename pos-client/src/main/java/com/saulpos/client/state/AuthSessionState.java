package com.saulpos.client.state;

import java.util.Set;

public record AuthSessionState(
        String username,
        String accessToken,
        String refreshToken,
        Set<String> permissions
) {
    public boolean isAuthenticated() {
        return accessToken != null && !accessToken.isBlank();
    }
}
