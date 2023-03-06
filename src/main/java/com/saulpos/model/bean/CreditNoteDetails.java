package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class CreditNoteDetails extends BeanImplementation<CreditNoteDetails> {

    private final SimpleIntegerProperty id = new SimpleIntegerProperty();

    private final ObjectProperty<CreditNote> creditNote = new SimpleObjectProperty<>();

    //codigo_de_articulo
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

    @ManyToOne
    public CreditNote getCreditNote() {
        return creditNote.get();
    }

    public ObjectProperty<CreditNote> creditNoteProperty() {
        return creditNote;
    }

    public void setCreditNote(CreditNote creditNote) {
        this.creditNote.set(creditNote);
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

    public int getAmount() {
        return amount.get();
    }

    public SimpleIntegerProperty amountProperty() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount.set(amount);
    }

    public int getCancelled() {
        return cancelled.get();
    }

    public SimpleIntegerProperty cancelledProperty() {
        return cancelled;
    }

    public void setCancelled(int cancelled) {
        this.cancelled.set(cancelled);
    }

    public double getSalePrice() {
        return salePrice.get();
    }

    public SimpleDoubleProperty salePriceProperty() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice.set(salePrice);
    }

    public double getDiscount() {
        return discount.get();
    }

    public SimpleDoubleProperty discountProperty() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount.set(discount);
    }

    @Override
    public void receiveChanges(CreditNoteDetails creditNoteDetails) {

    }

    @Override
    public CreditNoteDetails clone() {
        return null;
    }
}
