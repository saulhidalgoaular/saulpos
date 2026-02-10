package com.saulpos.api.giftcard;

import java.math.BigDecimal;
import java.time.Instant;

public record GiftCardTransactionResponse(
        Long id,
        GiftCardTransactionType transactionType,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        Long saleId,
        Long saleReturnId,
        String reference,
        String note,
        Instant createdAt
) {
}
