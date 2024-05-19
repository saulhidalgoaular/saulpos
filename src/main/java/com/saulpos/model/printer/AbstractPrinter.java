package com.saulpos.model.printer;

import com.saulpos.model.bean.CreditNote;
import com.saulpos.model.bean.Invoice;
import com.saulpos.model.bean.MoneyExtraction;
import com.saulpos.model.bean.PrinterInformation;

public interface AbstractPrinter {

    String calculateSerialNumber();

    void printInvoice(Invoice invoice);

    void printExtractMoneyTicket(MoneyExtraction moneyExtraction);

    void printZReport();

    String getLastFiscalNumber();

    void printCreditNote(CreditNote creditNote);

    void printXReport();

    void printNonFiscalInvoice();

    void printNonFiscalCreditNote();

    void printDaySummary();

    void openCommunication();

    void closeCommunication();

    PrinterInformation getInformation();
}
