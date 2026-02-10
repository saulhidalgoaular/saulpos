package com.saulpos.api.report;

import java.time.Instant;
import java.util.List;

public record InventoryMovementReportResponse(
        Instant from,
        Instant to,
        Long storeLocationId,
        Long categoryId,
        Long supplierId,
        List<InventoryMovementReportRowResponse> rows
) {
}
