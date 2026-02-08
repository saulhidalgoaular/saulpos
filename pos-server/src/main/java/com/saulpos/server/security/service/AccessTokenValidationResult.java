package com.saulpos.server.security.service;

public record AccessTokenValidationResult(
        AccessTokenValidationStatus status,
        AuthenticatedUser user
) {

    public static AccessTokenValidationResult authenticated(AuthenticatedUser user) {
        return new AccessTokenValidationResult(AccessTokenValidationStatus.AUTHENTICATED, user);
    }

    public static AccessTokenValidationResult failed(AccessTokenValidationStatus status) {
        return new AccessTokenValidationResult(status, null);
    }

    public boolean isAuthenticated() {
        return status == AccessTokenValidationStatus.AUTHENTICATED && user != null;
    }
}
