package com.saulpos.api.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

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
        List<InventoryMovementLotResponse> lots,
        BigDecimal runningBalance,
        Instant createdAt
) {
}
