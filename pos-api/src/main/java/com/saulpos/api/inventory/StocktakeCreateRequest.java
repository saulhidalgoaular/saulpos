package com.saulpos.api.inventory;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StocktakeCreateRequest(
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotEmpty(message = "productIds is required")
        List<@NotNull(message = "productId is required") Long> productIds,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
