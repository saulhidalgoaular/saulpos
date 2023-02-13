package com.saulpos.model.bean;


import jakarta.persistence.Entity;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

//ToDO check the attributes again
@Entity
@Table(name="barcode")
public class Barcode {

    @NotNull
    @Column(name = "product_id")
    private String productId;

    @Id @GeneratedValue
    @Column(name = "id")
    private int id;

    public Barcode(String productId, int id) {
        this.productId = productId;
        this.id = id;
    }

    public Barcode() {

    }

    public String getProductId() {
        return this.productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }
    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }



}