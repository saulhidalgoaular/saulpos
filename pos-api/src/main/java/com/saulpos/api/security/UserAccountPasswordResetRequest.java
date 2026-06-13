package com.saulpos.api.security;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserAccountPasswordResetRequest(
        @NotBlank(message = "newPassword is required")
        @Size(max = 255, message = "newPassword must be at most 255 characters")
        String newPassword
) {
}
