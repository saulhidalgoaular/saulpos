package com.saulpos.api.inventory;

import java.time.Instant;
import java.util.List;

public record StocktakeVarianceReportResponse(
        Long stocktakeId,
        Long storeLocationId,
        String referenceNumber,
        StocktakeStatus status,
        Instant snapshotAt,
        List<StocktakeLineResponse> byProduct,
        List<StocktakeCategoryVarianceResponse> byCategory
) {
}
