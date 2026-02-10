package com.saulpos.api.receipt;

import java.time.Instant;

public record CashDrawerOpenResponse(
        Long eventId,
        Long terminalDeviceId,
        String terminalCode,
        CashDrawerOpenStatus status,
        String adapter,
        boolean retryable,
        String message,
        Instant openedAt
) {
}
