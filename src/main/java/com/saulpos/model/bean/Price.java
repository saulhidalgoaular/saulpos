package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.Date;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Price extends AbstractBeanImplementation {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleObjectProperty<Product> product = new SimpleObjectProperty<>();

    private SimpleDoubleProperty price = new SimpleDoubleProperty();

    //Todo check the data type
    private final ObjectProperty<Date> date = new SimpleObjectProperty<>();

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

    @NotNull
    public Date getDate() {
        return this.date.get();
    }

    public void setDate(Date date) {
        this.date.set(date);
    }

    public @NotNull ObjectProperty<Date> dateProperty() {
        return date;
    }

    @Override
    public void receiveChanges(AbstractBeanImplementation currentBean) {
        //Todo
    }

    @Override
    public AbstractBeanImplementation clone() {
        //Todo
        return null;
    }
}
