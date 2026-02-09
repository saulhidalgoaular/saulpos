package com.saulpos.api.customer;

import com.saulpos.api.tax.TenderType;

import java.math.BigDecimal;
import java.time.Instant;

public record CustomerReturnsHistoryItemResponse(
        Long saleReturnId,
        Long saleId,
        String receiptNumber,
        String returnReference,
        String reasonCode,
        TenderType refundTenderType,
        BigDecimal totalGross,
        Instant returnedAt
) {
}
