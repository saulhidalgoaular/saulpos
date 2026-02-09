package com.saulpos.api.customer;

import java.util.List;

public record CustomerReturnsHistoryResponse(
        List<CustomerReturnsHistoryItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
