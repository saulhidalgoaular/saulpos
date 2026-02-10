package com.saulpos.api.inventory;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockTransferCreateLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "requestedQuantity is required")
        @Digits(integer = 10, fraction = 3, message = "requestedQuantity supports up to 10 integer digits and 3 decimals")
        BigDecimal requestedQuantity
) {
}
