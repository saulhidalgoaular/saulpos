package com.saulpos.api.report;

import java.time.Instant;
import java.util.List;

public record CashShiftReportResponse(
        Instant from,
        Instant to,
        Long storeLocationId,
        Long terminalDeviceId,
        Long cashierUserId,
        CashShiftReportSummaryResponse summary,
        List<CashShiftReportRowResponse> rows
) {
}
