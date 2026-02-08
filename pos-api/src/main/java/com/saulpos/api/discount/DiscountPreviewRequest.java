package com.saulpos.api.discount;

import com.saulpos.api.tax.TenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.List;

public record DiscountPreviewRequest(
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotBlank(message = "contextKey is required")
        @Size(max = 64, message = "contextKey must be at most 64 characters")
        String contextKey,
        @NotNull(message = "at is required")
        Instant at,
        TenderType tenderType,
        @NotEmpty(message = "lines are required")
        List<@Valid DiscountPreviewLineRequest> lines
) {
}
