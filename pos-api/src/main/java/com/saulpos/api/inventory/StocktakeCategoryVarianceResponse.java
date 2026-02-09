package com.saulpos.api.inventory;

import java.math.BigDecimal;

public record StocktakeCategoryVarianceResponse(
        Long categoryId,
        String categoryCode,
        String categoryName,
        BigDecimal expectedQuantity,
        BigDecimal countedQuantity,
        BigDecimal varianceQuantity
) {
}
