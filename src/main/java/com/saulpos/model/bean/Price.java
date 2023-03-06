package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Price extends BeanImplementation<Price> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    @ManyToOne
    private SimpleObjectProperty<Product> product = new SimpleObjectProperty<>();

    private SimpleDoubleProperty price = new SimpleDoubleProperty();

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

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

    @NotNull
    @OneToOne
    public Product getProduct() {
        return product.get();
    }

    public @NotNull SimpleObjectProperty<Product> productProperty() {
        return product;
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    public Double getPrice() {
        return price.get();
    }

    public void setPrice(Double price) {
        this.price.set(price);
    }

    public @NotNull SimpleDoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    @Override
    public void receiveChanges(Price currentBean) {

    }

    @Override
    public Price clone() {
        //Todo
        return null;
    }
}
