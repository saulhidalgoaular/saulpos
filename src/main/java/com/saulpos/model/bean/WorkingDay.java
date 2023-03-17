package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class WorkingDay extends BeanImplementation<WorkingDay> {
    private  ObjectProperty<Date> date = new SimpleObjectProperty<>();
    private SimpleStringProperty saleCode =new SimpleStringProperty();
    private SimpleDoubleProperty moneyCreditCard= new SimpleDoubleProperty();
    private SimpleDoubleProperty cash=new SimpleDoubleProperty();
    private SimpleDoubleProperty moneyDebitCard = new SimpleDoubleProperty();
    private SimpleDoubleProperty creditNotesMoney =new SimpleDoubleProperty();
    private SimpleIntegerProperty ZReport =new SimpleIntegerProperty();
    private SimpleDoubleProperty cashMoneyPrinter = new SimpleDoubleProperty();
    private SimpleDoubleProperty creditCardMoneyPrinter = new SimpleDoubleProperty();
    private SimpleDoubleProperty debitCardMoneyPrinter = new SimpleDoubleProperty();
    private SimpleDoubleProperty creditNotePrinter = new SimpleDoubleProperty();
    private SimpleBooleanProperty updateValues = new SimpleBooleanProperty();
    private ObjectProperty<LocalDateTime> dateTime = new SimpleObjectProperty<>();
    private SimpleDoubleProperty totalSales = new SimpleDoubleProperty();
    private SimpleStringProperty zReportNumber = new SimpleStringProperty();
    private SimpleObjectProperty<Cashier> printer = new SimpleObjectProperty();
    private SimpleObjectProperty<Invoice> lastInvoice = new SimpleObjectProperty();
    private SimpleIntegerProperty amountInvoices = new SimpleIntegerProperty();
    private SimpleObjectProperty<CreditNote> lastCreditNote = new SimpleObjectProperty();
    private SimpleIntegerProperty amountCreditNotes = new SimpleIntegerProperty();
    private SimpleBooleanProperty closed = new SimpleBooleanProperty();

    @NotNull
    public Date getDate() {
        return this.date.get();
    }

    public void setDate(Date date) {
        this.date.set(date);
    }

    public @NotNull ObjectProperty<Date> dateProperty() {
        return date;
    }

    public @NotNull String getSaleCode(){
        return saleCode.get();
    }

    public void setSaleCode(String saleCode){
        this.saleCode.set(saleCode);
    }

    public SimpleStringProperty saleCodeProperty(){
        return saleCode;
    }

    @NotNull
    @ColumnDefault("0.00")
    public Double getMoneyCreditCard(){
        return moneyCreditCard.get();
    }

    public void setMoneyCreditCard(Double moneyCreditCard){
        this.moneyCreditCard.set(moneyCreditCard);
    }

    public SimpleDoubleProperty moneyCreditCard(){
        return moneyCreditCard;
    }

    @NotNull
    @ColumnDefault("0.00")
    public Double getCash(){
        return cash.get();
    }

    public void setCash(Double cash){
        this.cash.set(cash);
    }

    public SimpleDoubleProperty cashProperty(){
        return cash;
    }

    @NotNull
    @ColumnDefault("0.00")
    public Double getMoneyDebitCard(){
        return moneyDebitCard.get();
    }

    public void setMoneyDebitCard(Double moneyDebitCard){
        this.moneyDebitCard.set(moneyDebitCard);
    }

    public SimpleDoubleProperty moneyDebitCardProperty(){
        return moneyDebitCard;
    }

    @NotNull
    @ColumnDefault("0.00")
    public Double getCreditNotesMoney(){
        return creditNotesMoney.get();
    }

    public void setCreditNotesMoney(Double creditNotesMoney){
        this.creditNotesMoney.set(creditNotesMoney);
    }

    public SimpleDoubleProperty creditNotesMoneyProperty(){
        return creditNotesMoney;
    }

    @ColumnDefault("0.00")
    public Double getCashMoneyPrinter(){
        return cashMoneyPrinter.get();
    }

    public void setCashMoneyPrinter(Double cashMoneyPrinter){
        this.cashMoneyPrinter.set(cashMoneyPrinter);
    }

    @NotNull
    @ColumnDefault("0.00")
    public  double getCreditCardMoneyPrinter(){
        return creditCardMoneyPrinter.get();
    }

    public void setCreditCardMoneyPrinter(Double creditCardMoneyPrinter){
        this.creditCardMoneyPrinter.set(creditCardMoneyPrinter);
    }

    public SimpleDoubleProperty creditCardMoneyPrinterProperty(){
        return creditCardMoneyPrinter;
    }

    @NotNull
    @ColumnDefault("0.00")
    public double getDebitCardMoneyPrinter(){
        return debitCardMoneyPrinter.get();
    }

    public void setDebitCardMoneyPrinter(Double debitCardMoneyPrinter){
        this.debitCardMoneyPrinter.set(debitCardMoneyPrinter);
    }

    public SimpleDoubleProperty debitCardMoneyPrinterProperty(){
        return debitCardMoneyPrinter;
    }

    @NotNull
    @ColumnDefault("0.00")
    public double getCreditNotePrinter(){
        return creditNotePrinter.get();
    }

    public void setCreditNotePrinter(Double creditNotePrinter){
        this.creditNotePrinter.set(creditNotePrinter);
    }

    public SimpleDoubleProperty creditNotePrinterProperty(){
        return creditNotePrinter;
    }

    public SimpleBooleanProperty updateValuesProperty() {
        return updateValues;
    }

    public Double getTotalSales(){
        return totalSales.get();
    }

    public void setTotalSales(Double totalSales){
        this.totalSales.set(totalSales);
    }

    public SimpleDoubleProperty totalSalesProperty(){
        return totalSales;
    }

    @ColumnDefault("0")
    @NotNull
    public String getzReportNumber(){
        return zReportNumber.get();
    }

    public void setzReportNumber(String zReportNumber){
        this.zReportNumber.set(zReportNumber);
    }

    public SimpleStringProperty zReportNumberProperty(){
        return zReportNumber;
    }

    @OneToOne
    public Cashier getPrinter() {
        return printer.get();
    }

    public SimpleObjectProperty<Cashier> printerProperty() {
        return printer;
    }

    public void setPrinter(Cashier printer) {
        this.printer.set(printer);
    }

    @OneToOne
    public Invoice getLastInvoice() {
        return lastInvoice.get();
    }

    public void setLastInvoice(Invoice lastInvoice) {
        this.lastInvoice.set(lastInvoice);
    }

    public SimpleObjectProperty<Invoice> lastInvoiceProperty() {
        return lastInvoice;
    }

    public int getAmountInvoices() {
        return amountInvoices.get();
    }

    public SimpleIntegerProperty amountInvoicesProperty() {
        return amountInvoices;
    }

    @OneToOne
    public CreditNote getLastCreditNote() {
        return lastCreditNote.get();
    }

    public SimpleObjectProperty<CreditNote> lastCreditNoteProperty() {
        return lastCreditNote;
    }

    public void setLastCreditNote(CreditNote lastCreditNote) {
        this.lastCreditNote.set(lastCreditNote);
    }

    public SimpleDoubleProperty moneyCreditCardProperty() {
        return moneyCreditCard;
    }

    public void setMoneyCreditCard(double moneyCreditCard) {
        this.moneyCreditCard.set(moneyCreditCard);
    }

    public void setCash(double cash) {
        this.cash.set(cash);
    }

    public void setMoneyDebitCard(double moneyDebitCard) {
        this.moneyDebitCard.set(moneyDebitCard);
    }

    public void setCreditNote(double creditNote) {
        this.creditNotesMoney.set(creditNote);
    }

    public int getZReport() {
        return ZReport.get();
    }

    public SimpleIntegerProperty ZReportProperty() {
        return ZReport;
    }

    public void setZReport(int ZReport) {
        this.ZReport.set(ZReport);
    }

    public SimpleDoubleProperty cashMoneyPrinterProperty() {
        return cashMoneyPrinter;
    }

    public void setCashMoneyPrinter(double cashMoneyPrinter) {
        this.cashMoneyPrinter.set(cashMoneyPrinter);
    }

    public void setCreditCardMoneyPrinter(double creditCardMoneyPrinter) {
        this.creditCardMoneyPrinter.set(creditCardMoneyPrinter);
    }

    public void setDebitCardMoneyPrinter(double debitCardMoneyPrinter) {
        this.debitCardMoneyPrinter.set(debitCardMoneyPrinter);
    }

    public void setCreditNotePrinter(double creditNotePrinter) {
        this.creditNotePrinter.set(creditNotePrinter);
    }

    public boolean isUpdateValues() {
        return updateValues.get();
    }

    public void setUpdateValues(boolean updateValues) {
        this.updateValues.set(updateValues);
    }

    public LocalDateTime getDateTime() {
        return dateTime.get();
    }

    public ObjectProperty<LocalDateTime> dateTimeProperty() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime.set(dateTime);
    }

    public void setTotalSales(double totalSales) {
        this.totalSales.set(totalSales);
    }


    public void setAmountInvoices(int amountInvoices) {
        this.amountInvoices.set(amountInvoices);
    }


    public int getAmountCreditNotes() {
        return amountCreditNotes.get();
    }

    public SimpleIntegerProperty amountCreditNotesProperty() {
        return amountCreditNotes;
    }

    public void setAmountCreditNotes(int amountCreditNotes) {
        this.amountCreditNotes.set(amountCreditNotes);
    }

    public void setClosed(boolean closed) {
        this.closed.set(closed);
    }

    @NotNull
    public Boolean isClosed(){
        return closed.get();
    }

    public void setClosed(Boolean closed){
        this.closed.set(closed);
    }

    public @NotNull SimpleBooleanProperty closedProperty(){
        return closed;
    }

}
