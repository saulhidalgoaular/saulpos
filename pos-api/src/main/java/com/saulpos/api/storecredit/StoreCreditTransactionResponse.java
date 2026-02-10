package com.saulpos.api.storecredit;

import java.math.BigDecimal;
import java.time.Instant;

public record StoreCreditTransactionResponse(
        Long id,
        StoreCreditTransactionType transactionType,
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
