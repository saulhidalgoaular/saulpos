package com.saulpos.api.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SupplierReturnCreateRequest(
        @NotNull(message = "supplierId is required")
        Long supplierId,
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotEmpty(message = "lines is required")
        List<@Valid SupplierReturnCreateLineRequest> lines,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
