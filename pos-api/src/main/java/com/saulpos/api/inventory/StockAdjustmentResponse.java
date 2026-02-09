package com.saulpos.api.inventory;

import java.math.BigDecimal;
import java.time.Instant;

public record StockAdjustmentResponse(
        Long id,
        Long storeLocationId,
        Long productId,
        BigDecimal quantityDelta,
        String reasonCode,
        StockAdjustmentStatus status,
        boolean approvalRequired,
        String referenceNumber,
        String requestNote,
        String approvalNote,
        String postNote,
        String requestedBy,
        Instant requestedAt,
        String approvedBy,
        Instant approvedAt,
        String postedBy,
        Instant postedAt,
        Long inventoryMovementId
) {
}
