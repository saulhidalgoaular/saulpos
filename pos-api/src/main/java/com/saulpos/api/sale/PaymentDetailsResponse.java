package com.saulpos.api.sale;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PaymentDetailsResponse(
        Long paymentId,
        Long cartId,
        Long saleId,
        PaymentStatus status,
        BigDecimal totalPayable,
        BigDecimal totalAllocated,
        BigDecimal totalTendered,
        BigDecimal changeAmount,
        List<SaleCheckoutPaymentResponse> allocations,
        List<PaymentTransitionResponse> transitions,
        Instant createdAt,
        Instant updatedAt
) {
}
