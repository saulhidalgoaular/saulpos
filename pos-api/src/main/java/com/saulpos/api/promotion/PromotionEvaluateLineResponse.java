package com.saulpos.api.promotion;

import java.math.BigDecimal;

public record PromotionEvaluateLineResponse(
        int lineNumber,
        Long productId,
        String sku,
        String name,
        BigDecimal quantity,
        BigDecimal originalUnitPrice,
        BigDecimal discountedUnitPrice,
        BigDecimal lineSubtotalBeforeDiscount,
        BigDecimal lineDiscountAmount,
        BigDecimal lineSubtotalAfterDiscount
) {
}
