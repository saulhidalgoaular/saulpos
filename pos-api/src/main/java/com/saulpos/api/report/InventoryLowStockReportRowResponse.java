package com.saulpos.api.report;

import java.math.BigDecimal;

public record InventoryLowStockReportRowResponse(
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
        BigDecimal minimumQuantity,
        BigDecimal shortageQuantity
) {
}
