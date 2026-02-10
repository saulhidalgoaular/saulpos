package com.saulpos.server.receipt.printing;

public interface ReceiptTemplateRenderer {

    String render(ReceiptPrintPayload payload);
}
