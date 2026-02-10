package com.saulpos.api.report;

import java.math.BigDecimal;

public record SalesReturnsReportBucketResponse(
        String key,
        Long id,
        String code,
        String label,
        BigDecimal salesQuantity,
        BigDecimal returnQuantity,
        BigDecimal salesNet,
        BigDecimal returnNet,
        BigDecimal salesTax,
        BigDecimal returnTax,
        BigDecimal salesGross,
        BigDecimal returnGross,
        BigDecimal netGross
) {
}
