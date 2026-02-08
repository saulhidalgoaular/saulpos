package com.saulpos.api.loyalty;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record LoyaltyEarnRequest(
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotNull(message = "customerId is required")
        Long customerId,
        @NotBlank(message = "reference is required")
        @Size(max = 80, message = "reference must be at most 80 characters")
        String reference,
        @NotNull(message = "saleGrossAmount is required")
        @DecimalMin(value = "0.01", message = "saleGrossAmount must be greater than 0")
        BigDecimal saleGrossAmount
) {
}
