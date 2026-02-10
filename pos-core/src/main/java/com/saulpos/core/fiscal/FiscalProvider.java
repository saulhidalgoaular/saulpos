package com.saulpos.core.fiscal;

public interface FiscalProvider {

    String providerCode();

    FiscalProviderResult issueInvoice(FiscalIssueInvoiceCommand command);

    FiscalProviderResult cancelInvoice(FiscalCancelInvoiceCommand command);

    FiscalProviderResult issueCreditNote(FiscalIssueCreditNoteCommand command);
}
