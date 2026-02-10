package com.saulpos.api.receipt;

import java.math.BigDecimal;
import java.time.Instant;

public record ReceiptJournalResponse(
        Long saleId,
        Long receiptHeaderId,
        String receiptNumber,
        Long storeLocationId,
        String storeLocationCode,
        Long terminalDeviceId,
        String terminalCode,
        Long cashierUserId,
        String cashierUsername,
        BigDecimal totalPayable,
        Instant soldAt
) {
}
