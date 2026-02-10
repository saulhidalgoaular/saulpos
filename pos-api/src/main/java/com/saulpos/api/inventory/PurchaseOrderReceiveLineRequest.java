package com.saulpos.api.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.util.List;

public record PurchaseOrderReceiveLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "receivedQuantity is required")
        @DecimalMin(value = "0.001", message = "receivedQuantity must be greater than zero")
        BigDecimal receivedQuantity,
        @NotNull(message = "unitCost is required")
        @DecimalMin(value = "0.0001", message = "unitCost must be greater than zero")
        BigDecimal unitCost,
        @Size(max = 200, message = "lots must have at most 200 entries")
        List<@Valid PurchaseOrderReceiveLotRequest> lots
) {
}
