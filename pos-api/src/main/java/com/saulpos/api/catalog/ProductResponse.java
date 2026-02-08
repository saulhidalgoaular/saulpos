package com.saulpos.api.catalog;

import java.util.List;

public record ProductResponse(
        Long id,
        Long merchantId,
        Long categoryId,
        String sku,
        String name,
        String description,
        boolean active,
        List<ProductVariantResponse> variants
) {
}
