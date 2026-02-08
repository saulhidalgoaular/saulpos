package com.saulpos.server.loyalty.service;

public record LoyaltyProviderRedeemCommand(
        Long merchantId,
        Long storeLocationId,
        Long customerId,
        String reference,
        Integer requestedPoints
) {
}
