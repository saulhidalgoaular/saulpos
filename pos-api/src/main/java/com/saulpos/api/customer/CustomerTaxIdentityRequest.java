package com.saulpos.api.customer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerTaxIdentityRequest(
        @NotBlank(message = "documentType is required")
        @Size(max = 40, message = "documentType must be at most 40 characters")
        String documentType,
        @NotBlank(message = "documentValue is required")
        @Size(max = 80, message = "documentValue must be at most 80 characters")
        String documentValue
) {
}
