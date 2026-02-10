package com.saulpos.api.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StockTransferReceiveRequest(
        @NotEmpty(message = "lines is required")
        List<@Valid StockTransferReceiveLineRequest> lines,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
