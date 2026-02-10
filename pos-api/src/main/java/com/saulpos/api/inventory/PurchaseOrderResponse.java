package com.saulpos.api.inventory;

import java.time.Instant;
import java.util.List;

public record PurchaseOrderResponse(
        Long id,
        Long supplierId,
        Long storeLocationId,
        String referenceNumber,
        PurchaseOrderStatus status,
        String note,
        String createdBy,
        Instant createdAt,
        String approvedBy,
        Instant approvedAt,
        String lastReceivedBy,
        Instant lastReceivedAt,
        List<PurchaseOrderLineResponse> lines,
        List<GoodsReceiptResponse> receipts
) {
}
