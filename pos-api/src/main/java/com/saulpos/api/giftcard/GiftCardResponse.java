package com.saulpos.api.giftcard;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record GiftCardResponse(
        Long id,
        Long merchantId,
        Long customerId,
        String cardNumber,
        GiftCardStatus status,
        BigDecimal issuedAmount,
        BigDecimal balanceAmount,
        Instant issuedAt,
        Instant createdAt,
        Instant updatedAt,
        List<GiftCardTransactionResponse> transactions
) {
}
