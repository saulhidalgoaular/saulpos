package com.saulpos.api.receipt;

import jakarta.validation.constraints.NotNull;

public record ReceiptAllocationRequest(
        @NotNull(message = "terminalDeviceId is required")
        Long terminalDeviceId
) {
}
