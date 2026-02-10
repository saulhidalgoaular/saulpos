package com.saulpos.api.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PurchaseOrderReceiveLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "receivedQuantity is required")
        @DecimalMin(value = "0.001", message = "receivedQuantity must be greater than zero")
        BigDecimal receivedQuantity
) {
}
