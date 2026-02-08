package com.saulpos.api.identity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TerminalDeviceRequest(
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotBlank(message = "code is required")
        @Size(max = 80, message = "code must be at most 80 characters")
        String code,
        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name
) {
}
