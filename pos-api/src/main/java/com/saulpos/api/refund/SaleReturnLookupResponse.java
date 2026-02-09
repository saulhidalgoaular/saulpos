package com.saulpos.api.refund;

import java.time.Instant;
import java.util.List;

public record SaleReturnLookupResponse(
        Long saleId,
        String receiptNumber,
        Long storeLocationId,
        Long terminalDeviceId,
        Instant soldAt,
        List<SaleReturnLookupLineResponse> lines
) {
}
