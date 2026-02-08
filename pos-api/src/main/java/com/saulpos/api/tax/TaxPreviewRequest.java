package com.saulpos.api.tax;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.List;

public record TaxPreviewRequest(
        @NotNull(message = "storeLocationId is required")
        Long storeLocationId,
        @NotNull(message = "at is required")
        Instant at,
        @NotEmpty(message = "lines are required")
        List<@Valid TaxPreviewLineRequest> lines
) {
}
