package com.saulpos.api.catalog;

import java.util.List;

public record ProductSearchResponse(
        List<ProductResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
