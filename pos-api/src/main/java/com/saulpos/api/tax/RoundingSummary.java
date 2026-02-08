package com.saulpos.api.tax;

import java.math.BigDecimal;

public record RoundingSummary(
        boolean applied,
        TenderType tenderType,
        RoundingMethod method,
        BigDecimal increment,
        BigDecimal originalAmount,
        BigDecimal roundedAmount,
        BigDecimal adjustment
) {
}
