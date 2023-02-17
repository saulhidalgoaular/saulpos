package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.Date;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class WorkingDay extends AbstractBeanImplementation<WorkingDay> {

    private SimpleStringProperty id =new SimpleStringProperty();

    private  ObjectProperty<Date> date = new SimpleObjectProperty<>();

    //codigo_punto_de_venta
    private SimpleStringProperty saleCode =new SimpleStringProperty();

    //dinero_tarjeta_credito
    @ColumnDefault("0.00")
    private SimpleDoubleProperty moneyCreditCard= new SimpleDoubleProperty();

    //dinero_efectivo
    @ColumnDefault("0.00")
    private SimpleDoubleProperty cash=new SimpleDoubleProperty();

    //dinero_tarjeta_debito
    @ColumnDefault("0.00")
    private SimpleDoubleProperty moneyDebitCard = new SimpleDoubleProperty();

    //nota_de_credito
    @ColumnDefault("0.00")
    private SimpleDoubleProperty creditNote=new SimpleDoubleProperty();

    @ColumnDefault("0")
    private SimpleIntegerProperty ZReport =new SimpleIntegerProperty();

    //dinero_efectivo_impresora
    @ColumnDefault("0.00")
    private SimpleDoubleProperty cashMoneyPrinter = new SimpleDoubleProperty();

    //dinero_tarjeta_credito_impresora
    @ColumnDefault("0.00")
    private SimpleDoubleProperty creditCardMoneyPrinter = new SimpleDoubleProperty();

    //dinero_tarjeta_debito_impresora
    @ColumnDefault("0.00")
    private SimpleDoubleProperty debitCardMoneyPrinter = new SimpleDoubleProperty();

    //nota_de_credito_impresora
    @ColumnDefault("0.00")
    private SimpleDoubleProperty creditNotePrinter = new SimpleDoubleProperty();

    //actualizar_valores
    private SimpleBooleanProperty updateValues = new SimpleBooleanProperty();

    //ultima_actualizacion
    private ObjectProperty<LocalDateTime> dateTime = new SimpleObjectProperty<>();

    //total_ventas
    private SimpleDoubleProperty totalSales = new SimpleDoubleProperty();

    //numero_reporte_z
    private SimpleStringProperty zReportNumber = new SimpleStringProperty();

    //impresora
    private SimpleStringProperty printer = new SimpleStringProperty();

    //codigo_ultima_factura
    private SimpleStringProperty lastInvoice = new SimpleStringProperty();

    //num_facturas
    private SimpleIntegerProperty amountInvoices = new SimpleIntegerProperty();

    //codigo_ultima_nota_credito
    private SimpleStringProperty codeLastCreditNote = new SimpleStringProperty();

    //numero_notas_credito
    private SimpleIntegerProperty amountCreditNotes = new SimpleIntegerProperty();

    //cerrado
    private SimpleBooleanProperty closed = new SimpleBooleanProperty();

    @Id
    @GeneratedValue
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public SimpleStringProperty idProperty() {
        return id;
    }
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

    public @NotNull Double getMoneyCreditCard(){
        return moneyCreditCard.get();
    }

    public void setMoneyCreditCard(Double moneyCreditCard){
        this.moneyCreditCard.set(moneyCreditCard);
    }

    public SimpleDoubleProperty moneyCreditCard(){
        return moneyCreditCard;
    }

    public @NotNull Double getCash(){
        return cash.get();
    }

    public void setCash(Double cash){
        this.cash.set(cash);
    }

    public SimpleDoubleProperty cashProperty(){
        return cash;
    }

    public @NotNull Double getMoneyDebitCard(){
        return moneyDebitCard.get();
    }

    public void setMoneyDebitCard(Double moneyDebitCard){
        this.moneyDebitCard.set(moneyDebitCard);
    }

    public SimpleDoubleProperty moneyDebitCardProperty(){
        return moneyDebitCard;
    }

    public @NotNull Double getCreditNote(){
        return creditNote.get();
    }

    public void setCreditNote(Double creditNote){
        this.creditNote.set(creditNote);
    }

    public SimpleDoubleProperty creditNoteProperty(){
        return creditNote;
    }

    public Double getCashMoneyPrinter(){
        return cashMoneyPrinter.get();
    }

    public void setCashMoneyPrinter(Double cashMoneyPrinter){
        this.cashMoneyPrinter.set(cashMoneyPrinter);
    }

    public @NotNull double getCreditCardMoneyPrinter(){
        return creditCardMoneyPrinter.get();
    }

    public void setCreditCardMoneyPrinter(Double creditCardMoneyPrinter){
        this.creditCardMoneyPrinter.set(creditCardMoneyPrinter);
    }

    public SimpleDoubleProperty creditCardMoneyPrinterProperty(){
        return creditCardMoneyPrinter;
    }

    public @NotNull double getDebitCardMoneyPrinter(){
        return debitCardMoneyPrinter.get();
    }

    public void setDebitCardMoneyPrinter(Double debitCardMoneyPrinter){
        this.debitCardMoneyPrinter.set(debitCardMoneyPrinter);
    }

    public SimpleDoubleProperty debitCardMoneyPrinterProperty(){
        return debitCardMoneyPrinter;
    }

    public @NotNull double getCreditNotePrinter(){
        return creditNotePrinter.get();
    }

    public void setCreditNotePrinter(Double creditNotePrinter){
        this.creditNotePrinter.set(creditNotePrinter);
    }

    public SimpleDoubleProperty creditNotePrinterProperty(){
        return creditNotePrinter;
    }

    public @NotNull Boolean isUpdatedValues(){
        return updateValues.get();
    }

    public void setUpdateValues(Boolean updateValues){
        this.updateValues.set(updateValues);
    }

    public SimpleBooleanProperty updateValuesProperty(){
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

    public String getzReportNumber(){
        return zReportNumber.get();
    }

    public void setzReportNumber(String zReportNumber){
        this.zReportNumber.set(zReportNumber);
    }

    public SimpleStringProperty zReportNumberProperty(){
        return zReportNumber;
    }

    public String getPrinter(){
        return printer.get();
    }

    public void setPrinter(String printer){
        this.printer.set(printer);
    }

    public SimpleStringProperty printerProperty(){
        return printer;
    }

    public String getLastInvoice(){
        return lastInvoice.get();
    }

    public void setLastInvoice(String lastInvoice){
        this.lastInvoice.set(lastInvoice);
    }

    public SimpleStringProperty lastInvoiceCall(){
        return lastInvoice;
    }

    public SimpleStringProperty codeLastCreditNoteProperty(){
        return codeLastCreditNote;
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
        this.creditNote.set(creditNote);
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

    public SimpleStringProperty lastInvoiceProperty() {
        return lastInvoice;
    }

    public int getAmountInvoices() {
        return amountInvoices.get();
    }

    public SimpleIntegerProperty amountInvoicesProperty() {
        return amountInvoices;
    }

    public void setAmountInvoices(int amountInvoices) {
        this.amountInvoices.set(amountInvoices);
    }

    public String getCodeLastCreditNote() {
        return codeLastCreditNote.get();
    }

    public void setCodeLastCreditNote(String codeLastCreditNote) {
        this.codeLastCreditNote.set(codeLastCreditNote);
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

    @Override
    public void receiveChanges(WorkingDay currentBean) {

    }

    @Override
    public WorkingDay clone() {
        //Todo
        return null;
    }
}
