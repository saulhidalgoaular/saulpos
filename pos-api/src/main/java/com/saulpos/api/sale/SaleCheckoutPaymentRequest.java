package com.saulpos.api.sale;

import com.saulpos.api.tax.TenderType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SaleCheckoutPaymentRequest(
        @NotNull(message = "tenderType is required")
        TenderType tenderType,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than zero")
        BigDecimal amount,
        @DecimalMin(value = "0.00", message = "tenderedAmount cannot be negative")
        BigDecimal tenderedAmount,
        @Size(max = 120, message = "reference must be at most 120 characters")
        String reference
) {
}
