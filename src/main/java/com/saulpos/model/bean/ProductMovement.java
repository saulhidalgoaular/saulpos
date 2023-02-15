package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
//TODO check the keys
//Todo check the not null default for amount & type

@Entity
@Access(AccessType.PROPERTY)
@Table
public class ProductMovement extends AbstractBeanImplementation {

    private SimpleStringProperty id = new SimpleStringProperty();

    private SimpleObjectProperty<Product> product = new SimpleObjectProperty<>();

    private SimpleIntegerProperty amount = new SimpleIntegerProperty();

    private SimpleStringProperty type = new SimpleStringProperty();

    @Id
    @GeneratedValue
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public @NotNull SimpleStringProperty idProperty() {
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

    public @NotNull int getAmount() {
        return amount.get();
    }

    public void setAmount(int amount) {
        this.amount.set(amount);
    }

    public SimpleIntegerProperty amountProperty() {
        return amount;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public ProductMovement() {

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
