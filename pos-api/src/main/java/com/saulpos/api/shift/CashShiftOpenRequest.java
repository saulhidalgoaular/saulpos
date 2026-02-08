package com.saulpos.api.shift;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CashShiftOpenRequest(
        @NotNull(message = "cashierUserId is required")
        Long cashierUserId,
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId,
        @NotNull(message = "openingCash is required")
        @DecimalMin(value = "0.00", message = "openingCash must be non-negative")
        @Digits(integer = 12, fraction = 2, message = "openingCash must have up to 12 integer digits and 2 decimals")
        BigDecimal openingCash
) {
}
