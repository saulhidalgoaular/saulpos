package com.saulpos.api.receipt;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CashDrawerOpenRequest(
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId,
        @NotBlank(message = "reasonCode is required")
        @Size(max = 40, message = "reasonCode must be at most 40 characters")
        String reasonCode,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note,
        @Size(max = 120, message = "referenceNumber must be at most 120 characters")
        String referenceNumber
) {
}
