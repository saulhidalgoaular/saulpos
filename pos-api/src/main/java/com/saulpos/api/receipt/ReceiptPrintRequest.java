package com.saulpos.api.receipt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReceiptPrintRequest(
        @NotBlank(message = "receiptNumber is required")
        @Size(max = 80, message = "receiptNumber must be at most 80 characters")
        String receiptNumber,
        boolean copy
) {
}
