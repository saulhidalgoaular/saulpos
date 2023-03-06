package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class CreditNote extends BeanImplementation<CreditNote> {


    public enum CreditNoteStatus {
        InProgress, Completed
    }

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleObjectProperty<Invoice> invoice = new SimpleObjectProperty<>();

    private SimpleObjectProperty<Invoice.InvoiceStatus> status = new SimpleObjectProperty<>();

    private ObjectProperty<LocalDateTime> creationDate = new SimpleObjectProperty<>();

    private ObjectProperty<LocalDateTime> printingDate = new SimpleObjectProperty<>();

    private SimpleDoubleProperty totalWithoutVat = new SimpleDoubleProperty();

    private SimpleDoubleProperty totalWithVat = new SimpleDoubleProperty();
    private SimpleDoubleProperty vat = new SimpleDoubleProperty();

    @OneToOne
    private SimpleObjectProperty<Cashier> printer = new SimpleObjectProperty<>();

    private SimpleStringProperty taxNumber = new SimpleStringProperty();

    @Column(nullable = false)
    private SimpleStringProperty zReportNumber = new SimpleStringProperty();

    private SimpleObjectProperty<User> user = new SimpleObjectProperty<>();


    private SimpleIntegerProperty articlesQuantity = new SimpleIntegerProperty();

    private SimpleObjectProperty<Assignment> assignment = new SimpleObjectProperty();

    private SimpleObjectProperty<Cashier> posIdentifier = new SimpleObjectProperty();


    private ObjectProperty<Set<CreditNoteDetails>> creditNoteDetails = new SimpleObjectProperty<>();

    @Id @GeneratedValue
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    @OneToOne
    public Invoice getInvoice() {
        return invoice.get();
    }

    public SimpleObjectProperty<Invoice> invoiceProperty() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice.set(invoice);
    }

    public Invoice.InvoiceStatus getStatus() {
        return status.get();
    }

    public SimpleObjectProperty<Invoice.InvoiceStatus> statusProperty() {
        return status;
    }

    public void setStatus(Invoice.InvoiceStatus status) {
        this.status.set(status);
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

    public double getVat() {
        return vat.get();
    }

    public SimpleDoubleProperty vatProperty() {
        return vat;
    }

    public void setVat(double vat) {
        this.vat.set(vat);
    }

    @OneToMany
    public Set<CreditNoteDetails> getCreditNoteDetails() {
        return creditNoteDetails.get();
    }

    public ObjectProperty<Set<CreditNoteDetails>> creditNoteDetailsProperty() {
        return creditNoteDetails;
    }

    public void setCreditNoteDetails(Set<CreditNoteDetails> creditNoteDetails) {
        this.creditNoteDetails.set(creditNoteDetails);
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

    public int getArticlesQuantity() {
        return articlesQuantity.get();
    }

    public SimpleIntegerProperty articlesQuantityProperty() {
        return articlesQuantity;
    }

    public void setArticlesQuantity(int articlesQuantity) {
        this.articlesQuantity.set(articlesQuantity);
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
    public Cashier getPosIdentifier() {
        return posIdentifier.get();
    }

    public SimpleObjectProperty<Cashier> posIdentifierProperty() {
        return posIdentifier;
    }

    public void setPosIdentifier(Cashier posIdentifier) {
        this.posIdentifier.set(posIdentifier);
    }

    @Override
    public void receiveChanges(CreditNote creditNote) {

    }

    @Override
    public CreditNote clone() {
        return null;
    }
}
