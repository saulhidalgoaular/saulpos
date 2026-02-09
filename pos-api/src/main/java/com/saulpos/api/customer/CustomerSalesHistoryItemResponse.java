package com.saulpos.api.customer;

import java.math.BigDecimal;
import java.time.Instant;

public record CustomerSalesHistoryItemResponse(
        Long saleId,
        String receiptNumber,
        Long storeLocationId,
        Long terminalDeviceId,
        BigDecimal subtotalNet,
        BigDecimal totalTax,
        BigDecimal totalGross,
        BigDecimal totalPayable,
        Instant soldAt
) {
}
