package com.saulpos.api.sale;

import jakarta.validation.constraints.Size;

public record PaymentTransitionRequest(
        @Size(max = 255, message = "note must be at most 255 characters")
        String note
) {
}
