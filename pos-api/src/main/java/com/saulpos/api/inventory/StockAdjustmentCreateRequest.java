package com.saulpos.api.inventory;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StockAdjustmentCreateRequest(
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "quantityDelta is required")
        @Digits(integer = 10, fraction = 3, message = "quantityDelta supports up to 10 integer digits and 3 decimals")
        BigDecimal quantityDelta,
        @NotBlank(message = "reasonCode is required")
        @Size(max = 40, message = "reasonCode must be at most 40 characters")
        String reasonCode,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
