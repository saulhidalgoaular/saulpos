package com.saulpos.api.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StocktakeFinalizeLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "countedQuantity is required")
        @Digits(integer = 10, fraction = 3,
                message = "countedQuantity supports up to 10 integer digits and 3 decimals")
        @DecimalMin(value = "0.000", message = "countedQuantity must be greater than or equal to zero")
        BigDecimal countedQuantity
) {
}
