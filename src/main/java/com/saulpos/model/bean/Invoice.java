package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Invoice extends AbstractBeanImplementation<Invoice> {

    public enum Status{
        Pedido, Anulada, Espera, Facturada
    }

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    //codigo_interno
    private SimpleStringProperty internalCode = new SimpleStringProperty();

    private SimpleObjectProperty<Status> status = new SimpleObjectProperty<>();

    private ObjectProperty<LocalDateTime> creationDate = new SimpleObjectProperty<>();

    private ObjectProperty<LocalDateTime> printingDate = new SimpleObjectProperty<>();

    private SimpleObjectProperty<Client> client = new SimpleObjectProperty();

    private SimpleDoubleProperty totalWithoutVat = new SimpleDoubleProperty();

    private SimpleDoubleProperty totalWithVat = new SimpleDoubleProperty();

    @ColumnDefault("0.0000")
    private SimpleDoubleProperty globalDiscount = new SimpleDoubleProperty();

    //iva
    private SimpleDoubleProperty vat = new SimpleDoubleProperty();

    private SimpleStringProperty printer = new SimpleStringProperty();

    private SimpleStringProperty taxNumber = new SimpleStringProperty();

    private SimpleStringProperty zReportNumber = new SimpleStringProperty();

    private SimpleObjectProperty<User> user = new SimpleObjectProperty<>();

    private SimpleObjectProperty<Product> articlesQuantity = new SimpleObjectProperty<>();

    private SimpleObjectProperty<Shift> shiftIdentifier = new SimpleObjectProperty();

    private SimpleObjectProperty<Cashier> posIdentifier = new SimpleObjectProperty();

    private SimpleStringProperty alternativeInternalCode = new SimpleStringProperty();

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getInternalCode() {
        return internalCode.get();
    }

    public SimpleStringProperty internalCodeProperty() {
        return internalCode;
    }

    public void setInternalCode(String internalCode) {
        this.internalCode.set(internalCode);
    }

    public LocalDateTime getCreationDate() {
        return creationDate.get();
    }

    public ObjectProperty<LocalDateTime> creationDateProperty() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate.set(creationDate);
    }

    public LocalDateTime getPrintingDate() {
        return printingDate.get();
    }

    public ObjectProperty<LocalDateTime> printingDateProperty() {
        return printingDate;
    }

    public void setPrintingDate(LocalDateTime printingDate) {
        this.printingDate.set(printingDate);
    }

    @OneToOne
    public Client getClient() {
        return client.get();
    }

    public SimpleObjectProperty<Client> clientProperty() {
        return client;
    }

    public void setClient(Client client) {
        this.client.set(client);
    }

    public double getTotalWithoutVat() {
        return totalWithoutVat.get();
    }

    public SimpleDoubleProperty totalWithoutVatProperty() {
        return totalWithoutVat;
    }

    public void setTotalWithoutVat(double totalWithoutVat) {
        this.totalWithoutVat.set(totalWithoutVat);
    }

    public double getTotalWithVat() {
        return totalWithVat.get();
    }

    public SimpleDoubleProperty totalWithVatProperty() {
        return totalWithVat;
    }

    public void setTotalWithVat(double totalWithVat) {
        this.totalWithVat.set(totalWithVat);
    }

    public double getGlobalDiscount() {
        return globalDiscount.get();
    }

    public SimpleDoubleProperty globalDiscountProperty() {
        return globalDiscount;
    }

    public void setGlobalDiscount(double globalDiscount) {
        this.globalDiscount.set(globalDiscount);
    }

    public double getVat() {
        return vat.get();
    }

    public SimpleDoubleProperty vatProperty() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat.set(vat);
    }

    public String getPrinter() {
        return printer.get();
    }

    public SimpleStringProperty printerProperty() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer.set(printer);
    }

    public String getTaxNumber() {
        return taxNumber.get();
    }

    public SimpleStringProperty taxNumberProperty() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber.set(taxNumber);
    }

    public String getzReportNumber() {
        return zReportNumber.get();
    }

    public SimpleStringProperty zReportNumberProperty() {
        return zReportNumber;
    }

    public void setzReportNumber(String zReportNumber) {
        this.zReportNumber.set(zReportNumber);
    }

    @OneToOne
    public User getUser() {
        return user.get();
    }

    public SimpleObjectProperty<User> userProperty() {
        return user;
    }

    public void setUser(User user) {
        this.user.set(user);
    }

    @OneToOne
    public Product getArticlesQuantity() {
        return articlesQuantity.get();
    }

    public SimpleObjectProperty<Product> articlesQuantityProperty() {
        return articlesQuantity;
    }

    public void setArticlesQuantity(Product articlesQuantity) {
        this.articlesQuantity.set(articlesQuantity);
    }

    @OneToOne
    public Shift getShiftIdentifier() {
        return shiftIdentifier.get();
    }

    public SimpleObjectProperty<Shift> shiftIdentifierProperty() {
        return shiftIdentifier;
    }

    public void setShiftIdentifier(Shift shiftIdentifier) {
        this.shiftIdentifier.set(shiftIdentifier);
    }

    @OneToOne
    public Cashier getPosIdentifier() {
        return posIdentifier.get();
    }

    public SimpleObjectProperty<Cashier> posIdentifierProperty() {
        return posIdentifier;
    }

    public void setPosIdentifier(Cashier posIdentifier) {
        this.posIdentifier.set(posIdentifier);
    }

    public String getAlternativeInternalCode() {
        return alternativeInternalCode.get();
    }

    public SimpleStringProperty alternativeInternalCodeProperty() {
        return alternativeInternalCode;
    }

    public void setAlternativeInternalCode(String alternativeInternalCode) {
        this.alternativeInternalCode.set(alternativeInternalCode);
    }

    @Enumerated(EnumType.STRING)
    public Status getStatus() {
        return status.get();
    }

    public SimpleObjectProperty<Status> statusProperty() {
        return status;
    }

    public void setStatus(Status status) {
        this.status.set(status);
    }


    @Override
    public void receiveChanges(Invoice currentBean) {

    }

    @Override
    public Invoice clone() {
        //Todo
        return null;
    }
}
