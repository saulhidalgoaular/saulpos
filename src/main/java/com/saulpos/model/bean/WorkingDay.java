package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Date;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class WorkingDay extends AbstractBeanImplementation {

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

    //Todo check the name of the attribute & add getter , setter for it
    @ColumnDefault("0")
    private SimpleIntegerProperty reporteZ=new SimpleIntegerProperty();

    //dinero_efectivo_impresora
    @ColumnDefault("0.00")
    private SimpleDoubleProperty cashMoneyPrinter = new SimpleDoubleProperty();

    //dinero_tarjeta_credito_impresora
    @ColumnDefault("0.00")
    private SimpleDoubleProperty moneyCreditCardPrinter= new SimpleDoubleProperty();

    //dinero_tarjeta_debito_impresora
    @ColumnDefault("0.00")
    private SimpleDoubleProperty moneyDebitCardPrinter = new SimpleDoubleProperty();

    //nota_de_credito_impresora
    @ColumnDefault("0.00")
    private SimpleDoubleProperty creditNotePrinter = new SimpleDoubleProperty();

    //actualizar_valores
    private SimpleBooleanProperty updateValues = new SimpleBooleanProperty();

    //ultima_actualizacion
    private ObjectProperty<Date> dateTime = new SimpleObjectProperty<>();

    //total_ventas
    //Todo make the default value to be null
    private SimpleDoubleProperty totalSales = new SimpleDoubleProperty();

    //numero_reporte_z
    private SimpleStringProperty reportzNumber = new SimpleStringProperty();

    //impresora
    private SimpleStringProperty printer = new SimpleStringProperty();

    //codigo_ultima_factura
    private SimpleStringProperty lastInvoiceCall = new SimpleStringProperty();

    //num_facturas
    private SimpleDoubleProperty invoicesNumber = new SimpleDoubleProperty();

    //codigo_ultima_nota_credito
    private SimpleIntegerProperty codeLastCreditNote = new SimpleIntegerProperty();

    //numero_notas_credito
    private SimpleDoubleProperty creditNotesNumber = new SimpleDoubleProperty();

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

    public SimpleDoubleProperty cachMoneyPrinterProperty(){
        return cashMoneyPrinter;
    }

    public @NotNull double getMoneyCreditCardPrinter(){
        return moneyCreditCardPrinter.get();
    }

    public void setMoneyCreditCardPrinter(Double moneyCreditCardPrinter){
        this.moneyCreditCardPrinter.set(moneyCreditCardPrinter);
    }

    public SimpleDoubleProperty moneyCreditCardPrinterProperty(){
        return moneyCreditCardPrinter;
    }

    public @NotNull double getMoneyDebitCardPrinter(){
        return moneyDebitCardPrinter.get();
    }

    public void setMoneyDebitCardPrinter(Double moneyDebitCardPrinter){
        this.moneyDebitCardPrinter.set(moneyDebitCardPrinter);
    }

    public SimpleDoubleProperty moneyDebitCardPrinterProperty(){
        return moneyDebitCardPrinter;
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

    public Date getDateTime() {
        return this.dateTime.get();
    }

    public void setDateTime(Date dateTime) {
        this.dateTime.set(dateTime);
    }

    public ObjectProperty<Date> dateTimeProperty() {
        return dateTime;
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

    public String getReportzNumber(){
        return reportzNumber.get();
    }

    public void setReportzNumber(String reportzNumber){
        this.reportzNumber.set(reportzNumber);
    }

    public SimpleStringProperty reportzNumberProperty(){
        return reportzNumber;
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

    public String getLastInvoiceCall(){
        return lastInvoiceCall.get();
    }

    public void setLastInvoiceCall(String lastInvoiceCall){
        this.lastInvoiceCall.set(lastInvoiceCall);
    }

    public SimpleStringProperty lastInvoiceCall(){
        return lastInvoiceCall;
    }

    public Double getInvoicesNumber(){
        return invoicesNumber.get();
    }

    public void setInvoicesNumber(Double invoicesNumber){
        this.invoicesNumber.set(invoicesNumber);
    }

    public SimpleDoubleProperty invoicesNumberProperty(){
        return invoicesNumber;
    }

    public int getCodeLastCreditNote(){
        return codeLastCreditNote.get();
    }

    public void setCodeLastCreditNote(int codeLastCreditNote){
        this.codeLastCreditNote.set(codeLastCreditNote);
    }

    public SimpleIntegerProperty codeLastCreditNoteProperty(){
        return codeLastCreditNote;
    }

    public Double getCreditNotesNumber(){
        return creditNotesNumber.get();
    }

    public void setCreditNotesNumber(Double creditNotesNumber){
        this.creditNotesNumber.set(creditNotesNumber);
    }

    public SimpleDoubleProperty creditNotesNumberProperty(){
        return creditNotesNumber;
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
    public void receiveChanges(AbstractBeanImplementation currentBean) {
        //Todo
    }

    @Override
    public AbstractBeanImplementation clone() {
        //Todo
        return null;
    }
}
