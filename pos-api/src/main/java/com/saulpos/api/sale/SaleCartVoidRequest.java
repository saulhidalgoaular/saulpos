package com.saulpos.api.sale;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaleCartVoidRequest(
        @NotNull(message = "cashierUserId is required")
        Long cashierUserId,
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId,
        @NotBlank(message = "reasonCode is required")
        @Size(max = 40, message = "reasonCode must be at most 40 characters")
        String reasonCode,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
