package com.saulpos.api.inventory;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SupplierReturnCreateLineRequest(
        @NotNull(message = "productId is required")
        Long productId,
        @NotNull(message = "returnQuantity is required")
        @Digits(integer = 10, fraction = 3, message = "returnQuantity supports up to 10 integer digits and 3 decimals")
        BigDecimal returnQuantity,
        @NotNull(message = "unitCost is required")
        @Digits(integer = 12, fraction = 4, message = "unitCost supports up to 12 integer digits and 4 decimals")
        BigDecimal unitCost
) {
}
