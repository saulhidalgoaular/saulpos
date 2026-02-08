package com.saulpos.api.loyalty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record LoyaltyRedeemRequest(
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotNull(message = "customerId is required")
        Long customerId,
        @NotBlank(message = "reference is required")
        @Size(max = 80, message = "reference must be at most 80 characters")
        String reference,
        @NotNull(message = "requestedPoints is required")
        @Positive(message = "requestedPoints must be greater than 0")
        Integer requestedPoints
) {
}
