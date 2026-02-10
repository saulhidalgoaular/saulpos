package com.saulpos.core.fiscal;

public record FiscalCancelInvoiceCommand(
        Long fiscalDocumentId,
        String externalDocumentId,
        String reason
) {
}
