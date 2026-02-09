package com.saulpos.api.supplier;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SupplierRequest(
        @NotNull(message = "merchantId is required")
        Long merchantId,
        @NotBlank(message = "code is required")
        @Size(max = 80, message = "code must be at most 80 characters")
        String code,
        @NotBlank(message = "name is required")
        @Size(max = 160, message = "name must be at most 160 characters")
        String name,
        @Size(max = 80, message = "taxIdentifier must be at most 80 characters")
        String taxIdentifier,
        Boolean active,
        List<@Valid SupplierContactRequest> contacts,
        @Valid SupplierTermsRequest terms
) {
}
