package com.saulpos.api.sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SaleCheckoutResponse(
        Long cartId,
        Long saleId,
        String receiptNumber,
        Long paymentId,
        BigDecimal totalPayable,
        BigDecimal totalAllocated,
        BigDecimal totalTendered,
        BigDecimal changeAmount,
        List<SaleCheckoutPaymentResponse> payments,
        Instant capturedAt
) {
}
