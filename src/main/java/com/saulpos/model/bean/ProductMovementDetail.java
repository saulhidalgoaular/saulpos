package com.saulpos.model.bean;


import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
//TODO check the keys

@Entity
@Access(AccessType.PROPERTY)
@Table
public class ProductMovementDetail extends BeanImplementation<ProductMovementDetail> {

    private SimpleStringProperty id = new SimpleStringProperty();
    private SimpleObjectProperty<Product> product = new SimpleObjectProperty<>();
    private SimpleIntegerProperty amount = new SimpleIntegerProperty();
    private SimpleStringProperty type = new SimpleStringProperty();
    private SimpleObjectProperty<ProductMovement> productMovement = new SimpleObjectProperty<ProductMovement>();

    @Id
    @GeneratedValue
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public SimpleStringProperty idProperty() {
        return id;
    }


    @OneToOne
    @NotNull
    public Product getProduct() {
        return product.get();
    }

    public SimpleObjectProperty<Product> productProperty() {
        return product;
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    @NotNull
    public int getAmount() {
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

    @ManyToOne
    @NotNull
    public ProductMovement getProductMovement() {
        return productMovement.get();
    }

    public SimpleObjectProperty<ProductMovement> productMovementProperty() {
        return productMovement;
    }

    public void setProductMovement(ProductMovement productMovement) {
        this.productMovement.set(productMovement);
    }

    public ProductMovementDetail() {

    }

    @Override
    public void receiveChanges(ProductMovementDetail currentBean) {

    }

    @Override
    public ProductMovementDetail clone() {
        //Todo
        return null;
    }
}
