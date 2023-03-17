package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.Set;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Invoice extends BeanImplementation<Invoice> {

    public enum InvoiceStatus {
        InProgress, Cancelled, Waiting, Completed
    }

    private SimpleObjectProperty<InvoiceStatus> status = new SimpleObjectProperty<>();

    private ObjectProperty<LocalDateTime> creationDate = new SimpleObjectProperty<>();

    private ObjectProperty<LocalDateTime> printingDate = new SimpleObjectProperty<>();

    private SimpleObjectProperty<Client> client = new SimpleObjectProperty();

    private SimpleDoubleProperty totalWithoutVat = new SimpleDoubleProperty();

    private SimpleDoubleProperty totalWithVat = new SimpleDoubleProperty();

    @ColumnDefault("0.0000")
    @NotNull
    private SimpleDoubleProperty globalDiscount = new SimpleDoubleProperty();

    //iva
    private SimpleDoubleProperty vat = new SimpleDoubleProperty();
    //@OneToOne
    @OneToOne
    private SimpleObjectProperty<Cashier> printer = new SimpleObjectProperty<>();

    private SimpleStringProperty taxNumber = new SimpleStringProperty();

    @Column(nullable = false)
    private SimpleStringProperty zReportNumber = new SimpleStringProperty();

    private SimpleObjectProperty<UserB> user = new SimpleObjectProperty<>();


    private SimpleIntegerProperty articlesQuantity = new SimpleIntegerProperty();

    private SimpleObjectProperty<Assignment> assignment = new SimpleObjectProperty();

    private SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty();

    @OneToOne
    public Cashier getPrinter() {
        return printer.get();
    }

    public ObjectProperty<Cashier> printerProperty() {
        return printer;
    }

    public void setPrinter(Cashier printer) {
        this.printer.set(printer);
    }
    public int getArticlesQuantity() {
        return articlesQuantity.get();
    }

    public SimpleIntegerProperty articlesQuantityProperty() {
        return articlesQuantity;
    }

    public void setArticlesQuantity(int articlesQuantity) {
        this.articlesQuantity.set(articlesQuantity);
    }

    @OneToMany
    public Set<InvoiceDetail> getInvoiceDetails() {
        return invoiceDetails.get();
    }

    public ObjectProperty<Set<InvoiceDetail>> invoiceDetailsProperty() {
        return invoiceDetails;
    }

    public void setInvoiceDetails(Set<InvoiceDetail> invoiceDetails) {
        this.invoiceDetails.set(invoiceDetails);
    }

    private ObjectProperty<Set<InvoiceDetail>> invoiceDetails;

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

    public String getTaxNumber() {
        return taxNumber.get();
    }

    public SimpleStringProperty taxNumberProperty() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber.set(taxNumber);
    }
    @Column(nullable = false)
    public String getzReportNumber() {
        return zReportNumber.get();
    }
    @Column(nullable = false)
    public SimpleStringProperty zReportNumberProperty() {
        return zReportNumber;
    }
    @Column(nullable = false)
    public void setzReportNumber(String zReportNumber) {
        this.zReportNumber.set(zReportNumber);
    }

    @OneToOne
    public UserB getUser() {
        return user.get();
    }

    public SimpleObjectProperty<UserB> userProperty() {
        return user;
    }

    public void setUser(UserB userB) {
        this.user.set(userB);
    }



    @OneToOne
    public Assignment getAssignment() {
        return assignment.get();
    }

    public SimpleObjectProperty<Assignment> assignmentProperty() {
        return assignment;
    }

    public void setAssignment(Assignment assignment) {
        this.assignment.set(assignment);
    }

    @OneToOne
    public Cashier getCashier() {
        return cashier.get();
    }

    public SimpleObjectProperty<Cashier> cashierProperty() {
        return cashier;
    }

    public void setCashier(Cashier cashier) {
        this.cashier.set(cashier);
    }

    @Enumerated(EnumType.STRING)
    public InvoiceStatus getStatus() {
        return status.get();
    }

    public SimpleObjectProperty<InvoiceStatus> statusProperty() {
        return status;
    }

    public void setStatus(InvoiceStatus status) {
        this.status.set(status);
    }

}
