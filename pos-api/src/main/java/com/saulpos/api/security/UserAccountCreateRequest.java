package com.saulpos.api.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserAccountCreateRequest(
        @NotBlank(message = "username is required")
        @Size(max = 80, message = "username must be at most 80 characters")
        String username,
        @NotBlank(message = "password is required")
        @Size(max = 255, message = "password must be at most 255 characters")
        String password
) {
}
