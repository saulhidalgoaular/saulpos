package com.saulpos.api.inventory;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record InventoryMovementCreateRequest(
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "movementType is required")
        InventoryMovementType movementType,
        @NotNull(message = "quantityDelta is required")
        @Digits(integer = 10, fraction = 3, message = "quantityDelta supports up to 10 integer digits and 3 decimals")
        BigDecimal quantityDelta,
        @NotNull(message = "referenceType is required")
        InventoryReferenceType referenceType,
        @NotBlank(message = "referenceNumber is required")
        @Size(max = 80, message = "referenceNumber must be at most 80 characters")
        String referenceNumber
) {
}
