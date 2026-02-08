package com.saulpos.api.security;

public record PermissionResponse(
        Long id,
        String code,
        String description
) {
}
