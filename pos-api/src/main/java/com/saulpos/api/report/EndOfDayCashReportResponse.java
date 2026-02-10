package com.saulpos.api.report;

import java.time.Instant;
import java.util.List;

public record EndOfDayCashReportResponse(
        Instant from,
        Instant to,
        Long storeLocationId,
        Long terminalDeviceId,
        Long cashierUserId,
        List<EndOfDayCashReportRowResponse> rows
) {
}
