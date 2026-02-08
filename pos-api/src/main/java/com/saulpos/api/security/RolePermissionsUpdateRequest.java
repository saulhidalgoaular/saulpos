package com.saulpos.api.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RolePermissionsUpdateRequest(
        Set<@NotBlank(message = "permission code is required")
            @Size(max = 80, message = "permission code must be at most 80 characters")
                    String> permissionCodes
) {
}
