package com.saulpos.api.catalog;

import java.util.Set;

public record ProductVariantResponse(
        Long id,
        String code,
        String name,
        boolean active,
        Set<String> barcodes
) {
}
