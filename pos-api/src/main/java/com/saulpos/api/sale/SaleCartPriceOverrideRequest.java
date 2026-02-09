package com.saulpos.api.sale;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SaleCartPriceOverrideRequest(
        @NotNull(message = "cashierUserId is required")
        Long cashierUserId,
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId,
        @NotNull(message = "unitPrice is required")
        @DecimalMin(value = "0.00", message = "unitPrice must be non-negative")
        @Digits(integer = 12, fraction = 2, message = "unitPrice must have up to 12 integer digits and 2 decimals")
        BigDecimal unitPrice,
        @NotBlank(message = "reasonCode is required")
        @Size(max = 40, message = "reasonCode must be at most 40 characters")
        String reasonCode,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
