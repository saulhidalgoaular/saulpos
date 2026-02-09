package com.saulpos.api.refund;

import java.math.BigDecimal;

public record SaleReturnLookupLineResponse(
        Long saleLineId,
        Long productId,
        int lineNumber,
        BigDecimal quantitySold,
        BigDecimal quantityReturned,
        BigDecimal quantityAvailable,
        BigDecimal unitPrice,
        BigDecimal grossAmount
) {
}
