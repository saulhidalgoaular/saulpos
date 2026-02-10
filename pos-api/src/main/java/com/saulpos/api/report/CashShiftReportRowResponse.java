package com.saulpos.api.report;

import com.saulpos.api.shift.CashShiftStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record CashShiftReportRowResponse(
        Long shiftId,
        Long storeLocationId,
        String storeLocationCode,
        String storeLocationName,
        Long terminalDeviceId,
        String terminalDeviceCode,
        String terminalDeviceName,
        Long cashierUserId,
        String cashierUsername,
        CashShiftStatus status,
        BigDecimal openingCash,
        BigDecimal totalPaidIn,
        BigDecimal totalPaidOut,
        BigDecimal expectedCloseCash,
        BigDecimal countedCloseCash,
        BigDecimal varianceCash,
        String varianceReason,
        Instant openedAt,
        Instant closedAt
) {
}
