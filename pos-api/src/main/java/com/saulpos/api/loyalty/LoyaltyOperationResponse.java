package com.saulpos.api.loyalty;

import java.time.Instant;

public record LoyaltyOperationResponse(
        Long eventId,
        LoyaltyOperationType operationType,
        LoyaltyOperationStatus status,
        Long storeLocationId,
        Long customerId,
        String reference,
        Integer pointsDelta,
        String providerCode,
        String providerReference,
        String message,
        Instant processedAt
) {
}
