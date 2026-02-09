package com.saulpos.api.sale;

import java.math.BigDecimal;
import java.time.Instant;

public record ParkedSaleCartSummaryResponse(
        Long cartId,
        String referenceCode,
        Long cashierUserId,
        Long storeLocationId,
        Long terminalDeviceId,
        Instant pricingAt,
        BigDecimal totalPayable,
        Instant parkedAt,
        Instant expiresAt,
        Instant updatedAt
) {
}
