package com.saulpos.api.catalog;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
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
        @DecimalMin(value = "0.00", message = "basePrice must be non-negative")
        @Digits(integer = 12, fraction = 2, message = "basePrice must have up to 12 integer digits and 2 decimals")
        BigDecimal basePrice,
        @Size(max = 255, message = "description must be at most 255 characters")
        String description,
        @NotNull(message = "variants are required")
        @Size(min = 1, message = "at least one product variant is required")
        List<@Valid ProductVariantRequest> variants
) {
}
