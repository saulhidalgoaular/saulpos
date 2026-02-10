package com.saulpos.api.report;

import com.saulpos.api.inventory.InventoryMovementType;
import com.saulpos.api.inventory.InventoryReferenceType;

import java.math.BigDecimal;
import java.time.Instant;

public record InventoryMovementReportRowResponse(
        Long movementId,
        Instant occurredAt,
        Long storeLocationId,
        String storeLocationCode,
        String storeLocationName,
        Long productId,
        String productSku,
        String productName,
        Long categoryId,
        String categoryCode,
        String categoryName,
        InventoryMovementType movementType,
        InventoryReferenceType referenceType,
        String referenceNumber,
        BigDecimal quantityDelta
) {
}
