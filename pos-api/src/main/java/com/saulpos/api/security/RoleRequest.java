package com.saulpos.api.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record RoleRequest(
        @NotBlank(message = "code is required")
        @Size(max = 80, message = "code must be at most 80 characters")
        String code,
        @Size(max = 255, message = "description must be at most 255 characters")
        String description,
        Set<@NotBlank(message = "permission code is required")
            @Size(max = 80, message = "permission code must be at most 80 characters")
                    String> permissionCodes
) {
}
