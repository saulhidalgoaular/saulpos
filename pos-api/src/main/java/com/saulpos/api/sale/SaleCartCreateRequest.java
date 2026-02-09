package com.saulpos.api.sale;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record SaleCartCreateRequest(
        @NotNull(message = "cashierUserId is required")
        Long cashierUserId,
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId,
        @NotNull(message = "pricingAt is required")
        Instant pricingAt
) {
}
