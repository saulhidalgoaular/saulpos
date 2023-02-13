package com.saulpos.model.bean;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;
//TODO check the keys

@Entity
@Table(name = "product_movements")
public class ProductMovement {
    @NotNull @Id
    @Column(name = "id")
    private String id;

    @NotNull
    @Column(name = "product_id")
    private String productId;

    @NotNull
    @Column(name = "amount")
    private int amount;

    //Todo check the not null default
    @NotNull
    @Column(name = "type",nullable = false)
    private String type;

    public ProductMovement(@NotNull String id, @NotNull String productId, @NotNull int amount, @NotNull String type) {
        this.id = id;
        this.productId = productId;
        this.amount = amount;
        this.type = type;
    }

    public ProductMovement() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
