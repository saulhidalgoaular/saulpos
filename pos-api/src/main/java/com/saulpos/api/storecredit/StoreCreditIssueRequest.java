package com.saulpos.api.storecredit;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record StoreCreditIssueRequest(
        @NotNull(message = "merchantId is required")
        Long merchantId,
        @NotNull(message = "customerId is required")
        Long customerId,
        @NotNull(message = "amount is required")
        @DecimalMin(value = "0.01", message = "amount must be greater than 0")
        BigDecimal amount,
        @NotNull(message = "saleReturnId is required")
        Long saleReturnId,
        @Size(max = 80, message = "reference must be at most 80 characters")
        String reference,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
