package com.saulpos.api.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProductRequest(
        @NotNull(message = "merchantId is required")
        Long merchantId,
        Long categoryId,
        @NotBlank(message = "sku is required")
        @Size(max = 80, message = "sku must be at most 80 characters")
        String sku,
        @NotBlank(message = "name is required")
        @Size(max = 160, message = "name must be at most 160 characters")
        String name,
        @Size(max = 255, message = "description must be at most 255 characters")
        String description,
        @NotNull(message = "variants are required")
        @Size(min = 1, message = "at least one product variant is required")
        List<@Valid ProductVariantRequest> variants
) {
}
