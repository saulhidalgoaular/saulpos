package com.saulpos.api.promotion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PromotionEvaluateResponse(
        Long storeLocationId,
        Instant at,
        List<PromotionEvaluateLineResponse> lines,
        BigDecimal subtotalBeforeDiscount,
        BigDecimal totalDiscount,
        BigDecimal subtotalAfterDiscount,
        PromotionAppliedResponse appliedPromotion
) {
}
