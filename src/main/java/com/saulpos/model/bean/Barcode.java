package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Barcode extends AbstractBeanImplementation {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleObjectProperty<Product> product = new SimpleObjectProperty<>();


    public Barcode() {

    }

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

    public @NotNull SimpleObjectProperty<Product> productProperty() {
        return product;
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    @Override
    public void receiveChanges(AbstractBeanImplementation currentBean) {
        //TODO
    }

    @Override
    public AbstractBeanImplementation clone() {
        //TODO
        return null;
    }

}