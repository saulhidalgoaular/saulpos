package com.saulpos.api.inventory;

import java.time.Instant;
import java.util.List;

public record StockTransferResponse(
        Long id,
        Long sourceStoreLocationId,
        Long destinationStoreLocationId,
        String referenceNumber,
        StockTransferStatus status,
        String note,
        String createdBy,
        Instant createdAt,
        String shippedBy,
        Instant shippedAt,
        String receivedBy,
        Instant receivedAt,
        List<StockTransferLineResponse> lines
) {
}
