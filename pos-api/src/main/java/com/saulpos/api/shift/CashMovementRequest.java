package com.saulpos.api.shift;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CashMovementRequest(
        @NotNull(message = "movementType is required")
        CashMovementType movementType,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.00", message = "amount must be non-negative")
        @Digits(integer = 12, fraction = 2, message = "amount must have up to 12 integer digits and 2 decimals")
        BigDecimal amount,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
