package com.saulpos.model.bean;

import jakarta.persistence.*;
import org.jetbrains.annotations.NotNull;

import javax.xml.crypto.Data;
import java.security.PublicKey;
import java.util.Date;

@Entity
@Table(name = "price")
public class Price {
    @Id @GeneratedValue
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "product_id")
    private String productID;

    @NotNull
    @Column(name = "price")
    private double price;

    @NotNull
    @Column(name = "date")
    private Date data;

    public Price(@NotNull String productID, @NotNull double price, @NotNull Date data) {
        this.productID = productID;
        this.price = price;
        this.data = data;
    }
    public Price() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }
}
