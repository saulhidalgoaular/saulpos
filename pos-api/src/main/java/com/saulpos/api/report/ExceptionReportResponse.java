package com.saulpos.api.report;

import java.time.Instant;
import java.util.List;

public record ExceptionReportResponse(
        Instant from,
        Instant to,
        Long storeLocationId,
        Long terminalDeviceId,
        Long cashierUserId,
        String reasonCode,
        ExceptionReportEventType eventType,
        List<ExceptionReportRowResponse> rows
) {
}
