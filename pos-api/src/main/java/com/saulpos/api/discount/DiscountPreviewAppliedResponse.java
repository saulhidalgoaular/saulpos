package com.saulpos.api.discount;

import java.math.BigDecimal;

public record DiscountPreviewAppliedResponse(
        Long discountId,
        DiscountScope scope,
        Long productId,
        DiscountType type,
        BigDecimal value,
        String reasonCode,
        BigDecimal discountAmount
) {
}
