package com.saulpos.model.printer;

import com.saulpos.model.bean.*;

import java.time.LocalDateTime;
import java.util.List;

public class SoutPrinter implements AbstractPrinter{

    @Override
    public String calculateSerialNumber() {
        System.out.println("Calculating serial number...");
        return "SN123456";
    }

    @Override
    public void printInvoice(Invoice invoice) {
        System.out.println("Printing invoice: " + invoice);
        System.out.println("Invoice status: " + invoice.getStatus().toString());
        System.out.println("Invoice created date: " + invoice.getCreationDate());
        System.out.println("Invoice printing date: " + LocalDateTime.now());
        System.out.println("---------Client Information---------");
        Client client = invoice.getClient();
        if(client != null){
            String clientInfo = "Name: "+client.getName() +"\nAddress: "+client.getAddress()+"\nPhone: " +client.getPhone();
            System.out.println(clientInfo);
        }else{
            System.out.println("No client found!");
        }
        System.out.println("---------Product Information---------");
        System.out.println("SerialNo    ProductName    Amount    Price");
        List<InvoiceDetail> invoiceDetails = invoice.getInvoiceDetails();
        int serial = 1;
        for(InvoiceDetail invoiceDetail: invoiceDetails){
            System.out.println(serial++ +"    "
                    +invoiceDetail.getProduct().getDescription()+"    "
                    +invoiceDetail.getAmount()+"    "
                    +invoiceDetail.getSalePrice()
            );
        }
        System.out.println("---------Payment & Discount---------");
        System.out.printf("Global discountA: %.2f\n", invoice.getGlobalDiscount());
        System.out.printf("Vat: %.2f\n", invoice.getVat());
        System.out.printf("Total without vat: %.2f\n", invoice.getSubtotal());
        System.out.printf("Total with vat: %.2f\n", invoice.getTotal());
        System.out.printf("Total in USD(discount applied): %.2f\n", invoice.getTotalInUSD());
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
