package com.saulpos.api.discount;

import java.math.BigDecimal;
import java.time.Instant;

public record DiscountApplyResponse(
        Long id,
        Long storeLocationId,
        String contextKey,
        DiscountScope scope,
        Long productId,
        DiscountType type,
        BigDecimal value,
        String reasonCode,
        String note,
        boolean managerApprovalRequired,
        String appliedByUsername,
        Instant appliedAt
) {
}
