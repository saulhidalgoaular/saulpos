package com.saulpos.api.sale;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SaleCartParkRequest(
        @NotNull(message = "cashierUserId is required")
        Long cashierUserId,
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
