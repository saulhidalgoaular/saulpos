package com.saulpos.api.sale;

import jakarta.validation.constraints.NotNull;

public record SaleCartResumeRequest(
        @NotNull(message = "cashierUserId is required")
        Long cashierUserId,
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId
) {
}
