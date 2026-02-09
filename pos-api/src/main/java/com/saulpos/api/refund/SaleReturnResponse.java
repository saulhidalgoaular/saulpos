package com.saulpos.api.refund;

import com.saulpos.api.tax.TenderType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleReturnResponse(
        Long saleReturnId,
        Long saleId,
        String receiptNumber,
        String returnReference,
        String reasonCode,
        TenderType refundTenderType,
        BigDecimal subtotalNet,
        BigDecimal totalTax,
        BigDecimal totalGross,
        List<SaleReturnLineResponse> lines,
        Instant createdAt
) {
}
