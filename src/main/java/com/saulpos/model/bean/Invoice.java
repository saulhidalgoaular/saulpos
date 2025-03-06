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

import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.model.dao.BeanImplementation;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.exception.SaulPosException;
import jakarta.persistence.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;
import java.util.List;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Invoice extends BeanImplementation<Invoice> {

    private static final int DEFAULT_AMOUNT = 1;

    // Constructor
    public Invoice() {
        initializeBindings();
        initializeListeners();
    }

    public Invoice(double dollarRate) {
        this();
        this.dollarRate.set(dollarRate);
    }

    // Bindings for dynamic updates
    private void initializeBindings() {
        subtotal.bind(Bindings.createDoubleBinding(
                () -> invoiceDetails.stream()
                        .mapToDouble(detail -> {
                            double price = detail.getSalePrice();
                            double discount = price * (globalDiscount.get() / 100);
                            return price - discount;
                        }).sum(),
                invoiceDetails, globalDiscount
        ));

        vat.bind(Bindings.createDoubleBinding(
                () -> invoiceDetails.stream()
                        .mapToDouble(detail -> {
                            double priceAfterDiscount = detail.getSalePrice() * (1 - globalDiscount.get() / 100);
                            return priceAfterDiscount * (detail.getProduct().getVat().getPercentage() / 100); // Adjusted for percentage from 1 to 100
                        }).sum(),
                invoiceDetails, globalDiscount
        ));

        total.bind(Bindings.createDoubleBinding(
                () -> subtotal.get() + vat.get(),
                subtotal, vat
        ));

        totalInUSD.bind(Bindings.createDoubleBinding(
                () -> dollarRate.get() > 0 ? total.get() / dollarRate.get() : total.get(),
                total, dollarRate
        ));
    }

    // Listeners for dynamic updates
    private void initializeListeners() {
        invoiceDetails.addListener((ListChangeListener<InvoiceDetail>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    recalculateTotals();
                }
            }
        });
    }

    private void recalculateTotals() {
        // This method is mostly redundant due to bindings, but you can force recalculations here if needed.
    }

    // Add and remove invoice details
    public void addInvoiceDetail(InvoiceDetail detail) {
        invoiceDetails.add(detail);
    }

    // Conversion helper
    public DoubleBinding convertToDollar(double localCurrency) {
        return Bindings.createDoubleBinding(() -> localCurrency / dollarRate.get(), dollarRate);
    }

    public enum InvoiceStatus {
        InProgress, Cancelled, Waiting, Completed
    }

    private final SimpleObjectProperty<InvoiceStatus> status = new SimpleObjectProperty<>(InvoiceStatus.InProgress);

    private SimpleDoubleProperty dollarRate = new SimpleDoubleProperty();

    private final ObjectProperty<LocalDateTime> creationDate = new SimpleObjectProperty<>(LocalDateTime.now());

    private final ObjectProperty<LocalDateTime> printingDate = new SimpleObjectProperty<>();

    private final SimpleObjectProperty<Client> client = new SimpleObjectProperty();

    private final SimpleDoubleProperty subtotal = new SimpleDoubleProperty();

    private final SimpleDoubleProperty total = new SimpleDoubleProperty();

    private final SimpleDoubleProperty totalInUSD = new SimpleDoubleProperty();

    private final SimpleDoubleProperty globalDiscount = new SimpleDoubleProperty(.0);

    //iva
    private final SimpleDoubleProperty vat = new SimpleDoubleProperty();
    //@OneToOne
    @OneToOne
    private final SimpleObjectProperty<Cashier> printer = new SimpleObjectProperty<>();

    private final SimpleStringProperty taxNumber = new SimpleStringProperty();

    private final SimpleStringProperty zReportNumber = new SimpleStringProperty();

    private final SimpleObjectProperty<UserB> user = new SimpleObjectProperty<>();


    private final SimpleIntegerProperty articlesQuantity = new SimpleIntegerProperty();

    private final SimpleObjectProperty<Assignment> assignment = new SimpleObjectProperty();

    private final SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty();

    private final ObservableList<InvoiceDetail> invoiceDetails = FXCollections.observableArrayList();

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
    public List<InvoiceDetail> getInvoiceDetails() {
        return invoiceDetails;
    }

    public void setInvoiceDetails(List<InvoiceDetail> invoiceDetails) {
        this.invoiceDetails.setAll(invoiceDetails);
    }

    @Transient
    public ObservableList<InvoiceDetail> getObservableInvoiceDetails() {
        return invoiceDetails;
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

    public double getSubtotal() {
        return subtotal.get();
    }

    public SimpleDoubleProperty subtotalProperty() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal.set(subtotal);
    }

    public double getTotal() {
        return total.get();
    }

    public SimpleDoubleProperty totalProperty() {
        return total;
    }

    public void setTotal(double total) {
        this.total.set(total);
    }

    public double getTotalInUSD() {
        return totalInUSD.get();
    }

    public SimpleDoubleProperty totalInUSDProperty() {
        return totalInUSD;
    }

    public void setTotalInUSD(double totalInUSD) {
        this.totalInUSD.set(totalInUSD);
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

    public String getzReportNumber() {
        return zReportNumber.get();
    }

    public SimpleStringProperty zReportNumberProperty() {
        return zReportNumber;
    }

    public void setzReportNumber(String zReportNumber) {
        this.zReportNumber.set(zReportNumber);
    }

    @ManyToOne
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

    public double getDollarRate() {
        return dollarRate.get();
    }

    public SimpleDoubleProperty dollarRateProperty() {
        return dollarRate;
    }

    public void setDollarRate(double dollarRate) {
        this.dollarRate.set(dollarRate);
    }

    public void addItemToInvoice(String barcode) throws Exception {
        Product product = fetchProductByBarcode(barcode);
        if (product == null || product.getExistence() <= 0) {
            throw new SaulPosException("Product not found or out of stock.");
        }

        InvoiceDetail detail = createInvoiceDetail(product, DEFAULT_AMOUNT);
        addInvoiceDetail(detail);
        detail.saveOrUpdate();

        // Update stock
        product.setExistence(product.getExistence() - 1);
        product.saveOrUpdate();

    }

    public InvoiceDetail createInvoiceDetail(Product product, int amount) {
        InvoiceDetail detail = new InvoiceDetail();
        detail.setInvoice(this);
        detail.setProduct(product);
        detail.setSalePrice(product.priceProperty().get());
        detail.setAmount(amount);
        detail.setDiscount(product.getCurrentDiscount().get());
        detail.setCreationTime(LocalDateTime.now());
        return detail;
    }

    public void removeInvoiceDetail(InvoiceDetail invoiceDetail) throws Exception {
        Product product = invoiceDetail.getProduct();
        product.setExistence(product.getExistence() + invoiceDetail.getAmount());
        invoiceDetails.remove(invoiceDetail);
        product.saveOrUpdate();
        this.saveOrUpdate();
        System.out.println("Invoice Details size: " + this.getInvoiceDetails().size());
    }

    private Product fetchProductByBarcode(String barcode) throws Exception {
        Product sample = new Product();
        sample.setBarcode(barcode);
        List results = DatabaseConnection.getInstance().listBySample(Product.class, sample, AbstractDataProvider.SearchType.EQUAL);
        return results.isEmpty() ? null : (Product) results.get(0);
    }
}
