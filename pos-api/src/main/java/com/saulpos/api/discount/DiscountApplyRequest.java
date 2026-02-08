package com.saulpos.api.discount;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DiscountApplyRequest(
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotBlank(message = "contextKey is required")
        @Size(max = 64, message = "contextKey must be at most 64 characters")
        String contextKey,
        @NotNull(message = "scope is required")
        DiscountScope scope,
        Long productId,
        @NotNull(message = "type is required")
        DiscountType type,
        @NotNull(message = "value is required")
        @DecimalMin(value = "0.01", message = "value must be greater than zero")
        BigDecimal value,
        @NotBlank(message = "reasonCode is required")
        @Size(max = 40, message = "reasonCode must be at most 40 characters")
        String reasonCode,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
