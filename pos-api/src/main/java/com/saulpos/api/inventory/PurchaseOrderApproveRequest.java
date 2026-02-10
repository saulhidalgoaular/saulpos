package com.saulpos.api.inventory;

import jakarta.validation.constraints.Size;

public record PurchaseOrderApproveRequest(
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
