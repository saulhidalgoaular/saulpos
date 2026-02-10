package com.saulpos.api.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StockTransferCreateRequest(
        @NotNull(message = "sourceStoreLocationId is required")
        Long sourceStoreLocationId,
        @NotNull(message = "destinationStoreLocationId is required")
        Long destinationStoreLocationId,
        @NotEmpty(message = "lines is required")
        List<@Valid StockTransferCreateLineRequest> lines,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
