package com.saulpos.core.fiscal;

import java.math.BigDecimal;

public record FiscalIssueCreditNoteCommand(
        Long saleReturnId,
        Long saleId,
        String returnReference,
        String receiptNumber,
        Long merchantId,
        Long storeLocationId,
        Long customerId,
        BigDecimal totalGross
) {
}
