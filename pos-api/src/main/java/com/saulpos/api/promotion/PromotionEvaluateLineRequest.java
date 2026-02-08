package com.saulpos.api.promotion;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PromotionEvaluateLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.001", inclusive = true, message = "quantity must be greater than zero")
        BigDecimal quantity,
        @DecimalMin(value = "0.00", inclusive = true, message = "unitPrice must be greater than or equal to zero")
        BigDecimal unitPrice
) {
}
