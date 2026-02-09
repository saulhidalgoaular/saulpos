package com.saulpos.api.inventory;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryMovementResponse(
        Long id,
        Long storeLocationId,
        Long productId,
        InventoryMovementType movementType,
        BigDecimal quantityDelta,
        InventoryReferenceType referenceType,
        String referenceNumber,
        Long saleId,
        Long saleLineId,
        BigDecimal runningBalance,
        Instant createdAt
) {
}
