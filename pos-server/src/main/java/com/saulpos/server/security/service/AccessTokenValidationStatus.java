package com.saulpos.server.security.service;

public enum AccessTokenValidationStatus {
    AUTHENTICATED,
    INVALID,
    EXPIRED,
    INACTIVE
}
