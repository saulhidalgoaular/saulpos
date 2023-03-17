package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class InvoiceDetail extends BeanImplementation<InvoiceDetail> {

    private final SimpleIntegerProperty id = new SimpleIntegerProperty();

    //codigo_de_articulo : internal invoice code
    private final ObjectProperty<Invoice> invoice = new SimpleObjectProperty<Invoice>();

    //codigo_de_articulo
    @OneToOne
    private final SimpleObjectProperty<Product> product = new SimpleObjectProperty<Product>();

    //cantidad
    @ColumnDefault("1")
    private final SimpleIntegerProperty amount = new SimpleIntegerProperty();

    //devuelto
    private final SimpleIntegerProperty cancelled = new SimpleIntegerProperty();

    //precio_venta
    @ColumnDefault("0.00")
    private final SimpleDoubleProperty salePrice = new SimpleDoubleProperty();

    //descuento
    @ColumnDefault("0.00")
    private final SimpleDoubleProperty discount = new SimpleDoubleProperty();

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    @OneToOne
    public Product getProduct() {
        return product.get();
    }

    public SimpleObjectProperty<Product> productProperty() {
        return product;
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    public @NotNull int getAmount() {
        return amount.get();
    }

    public void setAmount(int amount) {
        this.amount.set(amount);
    }

    public SimpleIntegerProperty amountProperty() {
        return amount;
    }

    public @NotNull int getCancelled() {
        return cancelled.get();
    }

    public void setCancelled(int cancelled) {
        this.cancelled.set(cancelled);
    }

    public SimpleIntegerProperty cancelledProperty() {
        return cancelled;
    }

    public @NotNull Double getSalePrice() {
        return salePrice.get();
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice.set(salePrice);
    }

    public SimpleDoubleProperty salePriceProperty() {
        return salePrice;
    }

    public @NotNull Double getDiscount() {
        return discount.get();
    }

    public void setDiscount(Double discount) {
        this.discount.set(discount);
    }

    public SimpleDoubleProperty discountProperty() {
        return discount;
    }

    @ManyToOne
    public Invoice getInvoice() {
        return invoice.get();
    }

    public ObjectProperty<Invoice> invoiceProperty() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice.set(invoice);
    }

    public void setSalePrice(double salePrice) {
        this.salePrice.set(salePrice);
    }

    public void setDiscount(double discount) {
        this.discount.set(discount);
    }

}
