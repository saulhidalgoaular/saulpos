package com.saulpos.api.refund;

import com.saulpos.api.tax.TenderType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record SaleReturnSubmitRequest(
        Long saleId,
        @Size(max = 80, message = "receiptNumber must be at most 80 characters")
        String receiptNumber,
        @NotBlank(message = "reasonCode is required")
        @Size(max = 80, message = "reasonCode must be at most 80 characters")
        String reasonCode,
        @NotNull(message = "refundTenderType is required")
        TenderType refundTenderType,
        @Size(max = 120, message = "refundReference must be at most 120 characters")
        String refundReference,
        @Size(max = 255, message = "note must be at most 255 characters")
        String note,
        @NotEmpty(message = "lines is required")
        List<@Valid SaleReturnSubmitLineRequest> lines
) {
}
