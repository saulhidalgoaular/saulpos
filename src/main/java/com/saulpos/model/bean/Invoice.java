package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.Set;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Invoice extends AbstractBeanImplementation<Invoice> {

    public enum Status{
        InProgress, Cancelled, Waiting, Completed
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
    @NotNull
    private SimpleDoubleProperty globalDiscount = new SimpleDoubleProperty();

    //iva
    private SimpleDoubleProperty vat = new SimpleDoubleProperty();
    //@OneToOne
    @OneToOne
    private SimpleObjectProperty<TaxPrinter> printer = new SimpleObjectProperty<TaxPrinter>();

    private SimpleStringProperty taxNumber = new SimpleStringProperty();

    @Column(nullable = false)
    private SimpleStringProperty zReportNumber = new SimpleStringProperty();

    private SimpleObjectProperty<User> user = new SimpleObjectProperty<>();


    private SimpleIntegerProperty articlesQuantity = new SimpleIntegerProperty();

    private SimpleObjectProperty<Assignment> assignement = new SimpleObjectProperty();

    private SimpleObjectProperty<Cashier> posIdentifier = new SimpleObjectProperty();

    private SimpleStringProperty alternativeInternalCode = new SimpleStringProperty();

    @OneToOne
    public TaxPrinter getPrinter() {
        return printer.get();
    }

    public ObjectProperty<TaxPrinter> printerProperty() {
        return printer;
    }

    public void setPrinter(TaxPrinter printer) {
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
    public Assignment getAssignement() {
        return assignement.get();
    }

    public SimpleObjectProperty<Assignment> assignementProperty() {
        return assignement;
    }

    public void setAssignement(Assignment assignement) {
        this.assignement.set(assignement);
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
