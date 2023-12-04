/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    private final SimpleObjectProperty<Invoice> invoice = new SimpleObjectProperty<>();

    private final SimpleObjectProperty<Invoice.InvoiceStatus> status = new SimpleObjectProperty<>();

    private final ObjectProperty<LocalDateTime> creationDate = new SimpleObjectProperty<>();

    private final ObjectProperty<LocalDateTime> printingDate = new SimpleObjectProperty<>();

    private final SimpleDoubleProperty totalWithoutVat = new SimpleDoubleProperty();

    private final SimpleDoubleProperty totalWithVat = new SimpleDoubleProperty();
    private final SimpleDoubleProperty vat = new SimpleDoubleProperty();

    //@OneToOne Damir H. -> removed since we already have cashier, wich has printer.
    //private SimpleObjectProperty<Cashier> printer = new SimpleObjectProperty<Cashier>();

    private final SimpleStringProperty taxNumber = new SimpleStringProperty();

    @Column(nullable = false)
    private final SimpleStringProperty zReportNumber = new SimpleStringProperty();

    private final SimpleObjectProperty<UserB> user = new SimpleObjectProperty<>();


    private final SimpleIntegerProperty articlesQuantity = new SimpleIntegerProperty();

    private final SimpleObjectProperty<Assignment> assignment = new SimpleObjectProperty();

    private final SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty();


    private final ObjectProperty<Set<CreditNoteDetails>> creditNoteDetails = new SimpleObjectProperty<>();

    @OneToOne
    @NotNull
    public Invoice getInvoice() {
        return invoice.get();
    }

    public SimpleObjectProperty<Invoice> invoiceProperty() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice.set(invoice);
    }

    @NotNull
    @Enumerated(EnumType.STRING)
    public Invoice.InvoiceStatus getStatus() {
        return status.get();
    }

    public SimpleObjectProperty<Invoice.InvoiceStatus> statusProperty() {
        return status;
    }

    public void setStatus(Invoice.InvoiceStatus status) {
        this.status.set(status);
    }

    @NotNull
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

    @NotNull
    public double getTotalWithoutVat() {
        return totalWithoutVat.get();
    }

    public SimpleDoubleProperty totalWithoutVatProperty() {
        return totalWithoutVat;
    }

    public void setTotalWithoutVat(double totalWithoutVat) {
        this.totalWithoutVat.set(totalWithoutVat);
    }

    @NotNull
    public double getTotalWithVat() {
        return totalWithVat.get();
    }

    public SimpleDoubleProperty totalWithVatProperty() {
        return totalWithVat;
    }

    public void setTotalWithVat(double totalWithVat) {
        this.totalWithVat.set(totalWithVat);
    }

    @NotNull
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
    @NotNull
    public UserB getUser() {
        return user.get();
    }

    public SimpleObjectProperty<UserB> userProperty() {
        return user;
    }

    public void setUser(UserB userB) {
        this.user.set(userB);
    }

    @NotNull
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
    @NotNull
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

}
