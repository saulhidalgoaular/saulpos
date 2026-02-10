package com.saulpos.api.receipt;

import java.time.Instant;

public record ReceiptPrintResponse(
        String receiptNumber,
        ReceiptPrintStatus status,
        String adapter,
        boolean retryable,
        String message,
        Instant printedAt
) {
}
