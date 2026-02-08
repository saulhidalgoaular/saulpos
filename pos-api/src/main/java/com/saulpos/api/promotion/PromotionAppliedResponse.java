package com.saulpos.api.promotion;

import java.math.BigDecimal;
import java.util.List;

public record PromotionAppliedResponse(
        Long promotionId,
        String code,
        String name,
        int priority,
        BigDecimal totalDiscount,
        List<String> explanations
) {
}
