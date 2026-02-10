package com.saulpos.api.inventory;

import java.time.Instant;

public record GoodsReceiptResponse(
        Long id,
        String receiptNumber,
        String receivedBy,
        Instant receivedAt,
        String note
) {
}
