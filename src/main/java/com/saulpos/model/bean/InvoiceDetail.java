package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class InvoiceDetail extends AbstractBeanImplementation<InvoiceDetail> {

    private final SimpleIntegerProperty id = new SimpleIntegerProperty();

    //codigo_de_articulo : internal invoice code
    //Todo: check this please
    @OneToMany
    private final ObjectProperty<Invoice> invoice = new SimpleObjectProperty<>();

    //codigo_de_articulo
    private final SimpleStringProperty itemCode = new SimpleStringProperty();

    //cantidad
    @ColumnDefault("1")
    private final SimpleIntegerProperty amount = new SimpleIntegerProperty();

    //devuelto
    private final SimpleIntegerProperty returned = new SimpleIntegerProperty();

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

    public @NotNull String getItemCode() {
        return itemCode.get();
    }

    public void setItemCode(String itemCode) {
        this.itemCode.set(itemCode);
    }

    public SimpleStringProperty itemCodeProperty() {
        return itemCode;
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

    public @NotNull int getReturned() {
        return returned.get();
    }

    public void setReturned(int returned) {
        this.returned.set(returned);
    }

    public SimpleIntegerProperty returnedProperty() {
        return returned;
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


    @Override
    public void receiveChanges(InvoiceDetail currentBean) {

    }

    @Override
    public InvoiceDetail clone() {
        return null;
    }
}
