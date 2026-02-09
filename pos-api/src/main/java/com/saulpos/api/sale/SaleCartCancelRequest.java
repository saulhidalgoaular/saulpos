package com.saulpos.api.sale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaleCartCancelRequest(
        @NotNull(message = "cashierUserId is required")
        Long cashierUserId,
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId,
        @NotBlank(message = "reason is required")
        @Size(max = 255, message = "reason must be at most 255 characters")
        String reason
) {
}
