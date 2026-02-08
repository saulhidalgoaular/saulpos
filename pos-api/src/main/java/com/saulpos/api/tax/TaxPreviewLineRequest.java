package com.saulpos.api.tax;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TaxPreviewLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.001", message = "quantity must be greater than zero")
        @Digits(integer = 10, fraction = 3, message = "quantity must have up to 10 integer digits and 3 decimals")
        BigDecimal quantity,
        @DecimalMin(value = "0.00", message = "unitPrice must be non-negative")
        @Digits(integer = 12, fraction = 2, message = "unitPrice must have up to 12 integer digits and 2 decimals")
        BigDecimal unitPrice
) {
}
