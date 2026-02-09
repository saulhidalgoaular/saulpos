package com.saulpos.api.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record StocktakeFinalizeRequest(
        @NotEmpty(message = "lines is required")
        List<@Valid StocktakeFinalizeLineRequest> lines
) {
}
