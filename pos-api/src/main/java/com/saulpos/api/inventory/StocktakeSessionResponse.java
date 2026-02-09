package com.saulpos.api.inventory;

import java.time.Instant;
import java.util.List;

public record StocktakeSessionResponse(
        Long id,
        Long storeLocationId,
        String referenceNumber,
        StocktakeStatus status,
        Instant snapshotAt,
        String note,
        String createdBy,
        Instant createdAt,
        String startedBy,
        Instant startedAt,
        String finalizedBy,
        Instant finalizedAt,
        List<StocktakeLineResponse> lines
) {
}
