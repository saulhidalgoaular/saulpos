package com.saulpos.api.inventory;

import java.math.BigDecimal;

public record PurchaseOrderLineResponse(
        Long productId,
        String productSku,
        String productName,
        BigDecimal orderedQuantity,
        BigDecimal receivedQuantity,
        BigDecimal remainingQuantity
) {
}
