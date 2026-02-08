package com.saulpos.api.receipt;

import java.time.Instant;

public record ReceiptAllocationResponse(
        Long receiptHeaderId,
        Long seriesId,
        Long storeLocationId,
        Long terminalDeviceId,
        String seriesCode,
        ReceiptNumberPolicy numberPolicy,
        Long number,
        String receiptNumber,
        Instant issuedAt
) {
}
