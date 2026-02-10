package com.saulpos.server.receipt.printing;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record ReceiptPrintPayload(
        String receiptNumber,
        String storeCode,
        String storeName,
        String terminalCode,
        String cashierUsername,
        Instant soldAt,
        List<Line> lines,
        BigDecimal subtotal,
        BigDecimal tax,
        BigDecimal total,
        boolean copy
) {

    public record Line(
            int lineNumber,
            String productName,
            BigDecimal quantity,
            BigDecimal unitPrice,
            BigDecimal grossAmount
    ) {
    }
}
