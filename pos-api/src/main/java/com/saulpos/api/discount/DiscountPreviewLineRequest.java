package com.saulpos.api.discount;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record DiscountPreviewLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.001", message = "quantity must be greater than zero")
        BigDecimal quantity,
        BigDecimal unitPrice
) {
}
