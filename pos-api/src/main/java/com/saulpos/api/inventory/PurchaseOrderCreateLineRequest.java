package com.saulpos.api.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseOrderCreateLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "orderedQuantity is required")
        @DecimalMin(value = "0.001", message = "orderedQuantity must be greater than zero")
        BigDecimal orderedQuantity
) {
}
