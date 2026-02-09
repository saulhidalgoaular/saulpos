package com.saulpos.api.inventory;

import java.math.BigDecimal;

public record InventoryStockBalanceResponse(
        Long storeLocationId,
        Long productId,
        BigDecimal quantityOnHand
) {
}
