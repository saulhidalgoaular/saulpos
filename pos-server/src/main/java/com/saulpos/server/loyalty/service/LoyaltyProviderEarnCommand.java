package com.saulpos.server.loyalty.service;

import java.math.BigDecimal;

public record LoyaltyProviderEarnCommand(
        Long merchantId,
        Long storeLocationId,
        Long customerId,
        String reference,
        BigDecimal saleGrossAmount
) {
}
