package com.saulpos.api.report;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EndOfDayCashReportRowResponse(
        LocalDate businessDate,
        Long storeLocationId,
        String storeLocationCode,
        String storeLocationName,
        long shiftCount,
        BigDecimal expectedCloseCash,
        BigDecimal countedCloseCash,
        BigDecimal varianceCash,
        List<EndOfDayCashVarianceReasonResponse> varianceReasons
) {
}
