package com.saulpos.api.report;

import java.math.BigDecimal;
import java.util.List;

public record InventoryLowStockReportResponse(
        Long storeLocationId,
        Long categoryId,
        Long supplierId,
        BigDecimal minimumQuantity,
        List<InventoryLowStockReportRowResponse> rows
) {
}
