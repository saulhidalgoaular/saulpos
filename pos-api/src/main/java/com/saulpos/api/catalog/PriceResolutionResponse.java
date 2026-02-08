package com.saulpos.api.catalog;

import java.math.BigDecimal;
import java.time.Instant;

public record PriceResolutionResponse(
        Long storeLocationId,
        Long productId,
        BigDecimal resolvedPrice,
        PriceResolutionSource source,
        Long sourceId,
        Instant effectiveFrom,
        Instant effectiveTo,
        Instant resolvedAt
) {
}
