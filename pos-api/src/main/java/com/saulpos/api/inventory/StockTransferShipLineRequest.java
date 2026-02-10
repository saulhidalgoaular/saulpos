package com.saulpos.api.inventory;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockTransferShipLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "shippedQuantity is required")
        @Digits(integer = 10, fraction = 3, message = "shippedQuantity supports up to 10 integer digits and 3 decimals")
        BigDecimal shippedQuantity
) {
}
