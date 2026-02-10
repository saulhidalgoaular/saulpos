package com.saulpos.api.inventory;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record SupplierReturnResponse(
        Long id,
        Long supplierId,
        Long storeLocationId,
        String referenceNumber,
        SupplierReturnStatus status,
        String note,
        BigDecimal totalCost,
        String createdBy,
        Instant createdAt,
        String approvedBy,
        Instant approvedAt,
        String postedBy,
        Instant postedAt,
        List<SupplierReturnLineResponse> lines
) {
}
