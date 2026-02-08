package com.saulpos.api.catalog;

import java.math.BigDecimal;
import java.util.List;

public record ProductResponse(
        Long id,
        Long merchantId,
        Long categoryId,
        String sku,
        String name,
        BigDecimal basePrice,
        ProductSaleMode saleMode,
        ProductUnitOfMeasure quantityUom,
        int quantityPrecision,
        BigDecimal openPriceMin,
        BigDecimal openPriceMax,
        boolean openPriceRequiresReason,
        String description,
        boolean active,
        List<ProductVariantResponse> variants
) {
}
