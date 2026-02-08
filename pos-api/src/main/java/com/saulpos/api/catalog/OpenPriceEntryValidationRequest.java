package com.saulpos.api.catalog;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record OpenPriceEntryValidationRequest(
        @NotNull(message = "enteredPrice is required")
        @DecimalMin(value = "0.00", message = "enteredPrice must be non-negative")
        @Digits(integer = 12, fraction = 2, message = "enteredPrice must have up to 12 integer digits and 2 decimals")
        BigDecimal enteredPrice,
        @Size(max = 255, message = "reason must be at most 255 characters")
        String reason
) {
}
