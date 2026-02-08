package com.saulpos.api.shift;

import java.math.BigDecimal;
import java.time.Instant;

public record CashShiftResponse(
        Long id,
        Long cashierUserId,
        Long terminalDeviceId,
        Long storeLocationId,
        CashShiftStatus status,
        BigDecimal openingCash,
        BigDecimal totalPaidIn,
        BigDecimal totalPaidOut,
        BigDecimal expectedCloseCash,
        BigDecimal countedCloseCash,
        BigDecimal varianceCash,
        Instant openedAt,
        Instant closedAt
) {
}
