package com.saulpos.api.inventory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record InventoryMovementLotResponse(
        Long inventoryLotId,
        String lotCode,
        LocalDate expiryDate,
        InventoryExpiryState expiryState,
        BigDecimal quantity
) {
}
