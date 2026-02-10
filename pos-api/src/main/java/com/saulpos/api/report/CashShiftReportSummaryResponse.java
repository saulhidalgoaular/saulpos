package com.saulpos.api.report;

import java.math.BigDecimal;

public record CashShiftReportSummaryResponse(
        long shiftCount,
        long closedShiftCount,
        long openShiftCount,
        BigDecimal openingCash,
        BigDecimal totalPaidIn,
        BigDecimal totalPaidOut,
        BigDecimal expectedCloseCash,
        BigDecimal countedCloseCash,
        BigDecimal varianceCash
) {
}
