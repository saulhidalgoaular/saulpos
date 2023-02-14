package com.saulpos.model.bean;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


//ToDO check the attributes again
@Entity
@Access(AccessType.PROPERTY)
@Table(name="barcode")
public class Barcode {

    @NotNull
    @Column(name = "product_id")
    private String productId;

    private int id;

    public Barcode() {

    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    @Id @GeneratedValue
    @Column(name = "id")
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }



}