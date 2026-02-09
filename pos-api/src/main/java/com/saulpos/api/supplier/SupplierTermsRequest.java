package com.saulpos.api.supplier;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record SupplierTermsRequest(
        @Min(value = 0, message = "paymentTermDays must be zero or greater")
        Integer paymentTermDays,
        @DecimalMin(value = "0.00", message = "creditLimit must be zero or greater")
        BigDecimal creditLimit,
        @Size(max = 255, message = "notes must be at most 255 characters")
        String notes
) {
}
