package com.saulpos.api.refund;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SaleReturnSubmitLineRequest(
        @NotNull(message = "saleLineId is required")
        Long saleLineId,
        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.001", message = "quantity must be greater than zero")
        BigDecimal quantity
) {
}
