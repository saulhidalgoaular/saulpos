package com.saulpos.api.report;

import java.math.BigDecimal;

public record SalesReturnsReportSummaryResponse(
        long saleCount,
        long returnCount,
        BigDecimal salesQuantity,
        BigDecimal returnQuantity,
        BigDecimal salesNet,
        BigDecimal returnNet,
        BigDecimal salesTax,
        BigDecimal returnTax,
        BigDecimal salesGross,
        BigDecimal returnGross,
        BigDecimal netGross,
        BigDecimal discountGross
) {
}
