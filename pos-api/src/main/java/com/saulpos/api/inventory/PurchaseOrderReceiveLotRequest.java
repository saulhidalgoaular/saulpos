package com.saulpos.api.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PurchaseOrderReceiveLotRequest(
        @NotBlank(message = "lotCode is required")
        @Size(max = 80, message = "lotCode must be at most 80 characters")
        String lotCode,
        LocalDate expiryDate,
        @NotNull(message = "quantity is required")
        @DecimalMin(value = "0.001", message = "quantity must be greater than zero")
        BigDecimal quantity
) {
}
