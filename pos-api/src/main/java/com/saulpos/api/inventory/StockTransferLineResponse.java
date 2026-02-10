package com.saulpos.api.inventory;

import java.math.BigDecimal;

public record StockTransferLineResponse(
        Long productId,
        String productSku,
        String productName,
        BigDecimal requestedQuantity,
        BigDecimal shippedQuantity,
        BigDecimal receivedQuantity,
        BigDecimal remainingQuantity
) {
}
