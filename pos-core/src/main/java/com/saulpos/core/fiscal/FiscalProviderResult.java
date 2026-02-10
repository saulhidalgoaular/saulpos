package com.saulpos.core.fiscal;

public record FiscalProviderResult(
        boolean success,
        String externalDocumentId,
        String message
) {
}
