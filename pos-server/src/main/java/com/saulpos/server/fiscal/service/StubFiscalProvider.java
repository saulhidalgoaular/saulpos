package com.saulpos.server.fiscal.service;

import com.saulpos.core.fiscal.FiscalCancelInvoiceCommand;
import com.saulpos.core.fiscal.FiscalIssueCreditNoteCommand;
import com.saulpos.core.fiscal.FiscalIssueInvoiceCommand;
import com.saulpos.core.fiscal.FiscalProvider;
import com.saulpos.core.fiscal.FiscalProviderResult;

public class StubFiscalProvider implements FiscalProvider {

    @Override
    public String providerCode() {
        return "STUB";
    }

    @Override
    public FiscalProviderResult issueInvoice(FiscalIssueInvoiceCommand command) {
        return new FiscalProviderResult(
                true,
                "INV-" + command.saleId(),
                "stub invoice issued");
    }

    @Override
    public FiscalProviderResult cancelInvoice(FiscalCancelInvoiceCommand command) {
        return new FiscalProviderResult(
                true,
                command.externalDocumentId(),
                "stub invoice cancelled");
    }

    @Override
    public FiscalProviderResult issueCreditNote(FiscalIssueCreditNoteCommand command) {
        return new FiscalProviderResult(
                true,
                "CN-" + command.saleReturnId(),
                "stub credit note issued");
    }
}
