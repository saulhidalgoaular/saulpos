package com.saulpos.api.refund;

import java.math.BigDecimal;

public record SaleReturnLineResponse(
        Long saleReturnLineId,
        Long saleLineId,
        Long productId,
        int lineNumber,
        BigDecimal quantity,
        BigDecimal netAmount,
        BigDecimal taxAmount,
        BigDecimal grossAmount
) {
}
