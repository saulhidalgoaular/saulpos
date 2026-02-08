package com.saulpos.api.tax;

import java.math.BigDecimal;

public record TaxPreviewLineResponse(
        int lineNumber,
        Long productId,
        String sku,
        String productName,
        BigDecimal quantity,
        BigDecimal unitPrice,
        String taxGroupCode,
        TaxMode taxMode,
        BigDecimal taxRatePercent,
        boolean exempt,
        boolean zeroRated,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal grossAmount
) {
}
