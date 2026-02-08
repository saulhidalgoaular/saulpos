package com.saulpos.server.loyalty.service;

import com.saulpos.api.loyalty.LoyaltyOperationStatus;

public record LoyaltyProviderResult(
        LoyaltyOperationStatus status,
        Integer pointsDelta,
        String providerReference,
        String message
) {
}
