package com.saulpos.api.sale;

import com.saulpos.api.tax.RoundingSummary;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleCartResponse(
        Long id,
        Long cashierUserId,
        Long storeLocationId,
        Long terminalDeviceId,
        SaleCartStatus status,
        Instant pricingAt,
        List<SaleCartLineResponse> lines,
        BigDecimal subtotalNet,
        BigDecimal totalTax,
        BigDecimal totalGross,
        BigDecimal roundingAdjustment,
        BigDecimal totalPayable,
        RoundingSummary rounding,
        Instant createdAt,
        Instant updatedAt
) {
}
