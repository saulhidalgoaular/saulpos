package com.saulpos.api.shift;

import java.math.BigDecimal;
import java.time.Instant;

public record CashMovementResponse(
        Long id,
        Long shiftId,
        CashMovementType movementType,
        BigDecimal amount,
        String note,
        Instant occurredAt
) {
}
