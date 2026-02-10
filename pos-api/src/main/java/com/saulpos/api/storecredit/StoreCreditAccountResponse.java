package com.saulpos.api.storecredit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record StoreCreditAccountResponse(
        Long id,
        Long merchantId,
        Long customerId,
        BigDecimal balanceAmount,
        Instant createdAt,
        Instant updatedAt,
        List<StoreCreditTransactionResponse> transactions
) {
}
