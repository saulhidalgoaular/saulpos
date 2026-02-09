package com.saulpos.api.sale;

import java.time.Instant;

public record PaymentTransitionResponse(
        PaymentTransitionAction action,
        PaymentStatus fromStatus,
        PaymentStatus toStatus,
        String actorUsername,
        String note,
        String correlationId,
        Instant at
) {
}
