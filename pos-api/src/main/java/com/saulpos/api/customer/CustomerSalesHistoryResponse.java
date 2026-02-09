package com.saulpos.api.customer;

import java.util.List;

public record CustomerSalesHistoryResponse(
        List<CustomerSalesHistoryItemResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}
