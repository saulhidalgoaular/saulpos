package com.saulpos.api.sale;

import com.saulpos.api.tax.TenderType;

import java.math.BigDecimal;

public record SaleCheckoutPaymentResponse(
        int sequenceNumber,
        TenderType tenderType,
        BigDecimal amount,
        BigDecimal tenderedAmount,
        BigDecimal changeAmount,
        String reference
) {
}
