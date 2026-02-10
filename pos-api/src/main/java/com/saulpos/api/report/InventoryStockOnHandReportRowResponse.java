package com.saulpos.api.report;

import java.math.BigDecimal;

public record InventoryStockOnHandReportRowResponse(
        Long storeLocationId,
        String storeLocationCode,
        String storeLocationName,
        Long productId,
        String productSku,
        String productName,
        Long categoryId,
        String categoryCode,
        String categoryName,
        BigDecimal quantityOnHand,
        BigDecimal weightedAverageCost,
        BigDecimal lastCost,
        BigDecimal stockValue
) {
}
