package com.saulpos.api.supplier;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SupplierContactRequest(
        @NotNull(message = "contactType is required")
        SupplierContactType contactType,
        @NotBlank(message = "contactValue is required")
        @Size(max = 160, message = "contactValue must be at most 160 characters")
        String contactValue,
        Boolean primary
) {
}
