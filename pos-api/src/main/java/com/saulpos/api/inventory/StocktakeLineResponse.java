package com.saulpos.api.inventory;

import java.math.BigDecimal;

public record StocktakeLineResponse(
        Long productId,
        String productSku,
        String productName,
        Long categoryId,
        String categoryCode,
        String categoryName,
        BigDecimal expectedQuantity,
        BigDecimal countedQuantity,
        BigDecimal varianceQuantity,
        Long inventoryMovementId
) {
}
