package com.saulpos.api.report;

import java.util.List;

public record InventoryStockOnHandReportResponse(
        Long storeLocationId,
        Long categoryId,
        Long supplierId,
        List<InventoryStockOnHandReportRowResponse> rows
) {
}
