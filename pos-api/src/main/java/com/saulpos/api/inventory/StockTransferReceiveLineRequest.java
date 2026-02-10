package com.saulpos.api.inventory;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockTransferReceiveLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "receivedQuantity is required")
        @Digits(integer = 10, fraction = 3, message = "receivedQuantity supports up to 10 integer digits and 3 decimals")
        BigDecimal receivedQuantity
) {
}
