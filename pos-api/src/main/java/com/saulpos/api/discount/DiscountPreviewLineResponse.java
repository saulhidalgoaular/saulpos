package com.saulpos.api.discount;

import java.math.BigDecimal;

public record DiscountPreviewLineResponse(
        int lineNumber,
        Long productId,
        String sku,
        String productName,
        BigDecimal quantity,
        BigDecimal originalUnitPrice,
        BigDecimal discountedUnitPrice,
        BigDecimal lineSubtotalBeforeDiscount,
        BigDecimal lineDiscountAmount,
        BigDecimal lineSubtotalAfterDiscount
) {
}
