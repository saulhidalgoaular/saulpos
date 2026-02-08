package com.saulpos.api.catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record ProductVariantRequest(
        @NotBlank(message = "code is required")
        @Size(max = 80, message = "code must be at most 80 characters")
        String code,
        @NotBlank(message = "name is required")
        @Size(max = 160, message = "name must be at most 160 characters")
        String name,
        @NotNull(message = "barcodes are required")
        @Size(min = 1, message = "at least one barcode is required")
        Set<@NotBlank(message = "barcode is required")
            @Size(max = 64, message = "barcode must be at most 64 characters")
                    String> barcodes
) {
}
