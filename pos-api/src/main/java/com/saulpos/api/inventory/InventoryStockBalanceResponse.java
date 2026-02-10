package com.saulpos.api.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryStockBalanceResponse(
        Long storeLocationId,
        Long productId,
        BigDecimal quantityOnHand,
        Long inventoryLotId,
        String lotCode,
        LocalDate expiryDate,
        InventoryExpiryState expiryState,
        BigDecimal weightedAverageCost,
        BigDecimal lastCost
) {
}
