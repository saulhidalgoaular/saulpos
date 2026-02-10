package com.saulpos.api.inventory;

import java.math.BigDecimal;

public record SupplierReturnLineResponse(
        Long productId,
        String productSku,
        String productName,
        BigDecimal returnQuantity,
        BigDecimal unitCost,
        BigDecimal lineTotal,
        Long inventoryMovementId
) {
}
