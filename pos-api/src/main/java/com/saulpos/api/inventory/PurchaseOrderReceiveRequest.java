package com.saulpos.api.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record PurchaseOrderReceiveRequest(
        @NotEmpty(message = "lines is required")
        List<@Valid PurchaseOrderReceiveLineRequest> lines,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
