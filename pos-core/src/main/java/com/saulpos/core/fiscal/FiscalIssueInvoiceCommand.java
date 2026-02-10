package com.saulpos.core.fiscal;

import java.math.BigDecimal;

public record FiscalIssueInvoiceCommand(
        Long saleId,
        String receiptNumber,
        Long merchantId,
        Long storeLocationId,
        Long customerId,
        BigDecimal totalPayable
) {
}
