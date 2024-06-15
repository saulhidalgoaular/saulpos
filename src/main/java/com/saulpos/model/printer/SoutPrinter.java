package com.saulpos.model.printer;

import com.saulpos.model.bean.CreditNote;
import com.saulpos.model.bean.Invoice;
import com.saulpos.model.bean.MoneyExtraction;
import com.saulpos.model.bean.PrinterInformation;

public class SoutPrinter implements AbstractPrinter{

    @Override
    public String calculateSerialNumber() {
        System.out.println("Calculating serial number...");
        return "SN123456";
    }

    @Override
    public void printInvoice(Invoice invoice) {
        System.out.println("Printing invoice: " + invoice);
    }

    @Override
    public void printExtractMoneyTicket(MoneyExtraction moneyExtraction) {
        System.out.println("Printing money extraction ticket: " + moneyExtraction);
    }

    @Override
    public void printZReport() {
        System.out.println("Printing Z report...");
    }

    @Override
    public String getLastFiscalNumber() {
        System.out.println("Getting last fiscal number...");
        return "FN654321";
    }

    @Override
    public void printCreditNote(CreditNote creditNote) {
        System.out.println("Printing credit note: " + creditNote);
    }

    @Override
    public void printXReport() {
        System.out.println("Printing X report...");
    }

    @Override
    public void printNonFiscalInvoice() {
        System.out.println("Printing non-fiscal invoice...");
    }

    @Override
    public void printNonFiscalCreditNote() {
        System.out.println("Printing non-fiscal credit note...");
    }

    @Override
    public void printDaySummary() {
        System.out.println("Printing day summary...");
    }

    @Override
    public void openCommunication() {
        System.out.println("Opening communication...");
    }

    @Override
    public void closeCommunication() {
        System.out.println("Closing communication...");
    }

    @Override
    public PrinterInformation getInformation() {
        System.out.println("Getting printer information...");
        return new PrinterInformation(); // Assuming PrinterInformation has a default constructor
    }
}
