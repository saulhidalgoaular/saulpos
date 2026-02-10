package com.saulpos.api.report;

import java.time.Instant;

public record ExceptionReportRowResponse(
        Long eventId,
        Instant occurredAt,
        ExceptionReportEventType eventType,
        Long storeLocationId,
        String storeLocationCode,
        String storeLocationName,
        Long terminalDeviceId,
        String terminalDeviceCode,
        String terminalDeviceName,
        Long cashierUserId,
        String cashierUsername,
        String actorUsername,
        String approverUsername,
        String reasonCode,
        String note,
        String correlationId,
        String referenceNumber
) {
}
