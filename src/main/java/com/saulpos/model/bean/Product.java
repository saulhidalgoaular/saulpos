package com.saulpos.model.bean;

import javafx.beans.property.SimpleIntegerProperty;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import javax.persistence.*;
//Todo: check the constraints again
//Todo: check the primary key


@Entity
@Table(name = "product")
public class Product {
    @Id // TODO Confirm, I think it should be int
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "description")
    private String description;

    @NotNull
    @Column(name = "register_date")
    private Date registerDate;

    //Todo check the not null default
    @NotNull
    @Column(name = "brand",nullable = false)
    private String brand;

    //Todo check the not null default
    @NotNull
    @Column(name = "area",nullable = false)
    private String area;

    @NotNull
    @Column(name = "barcode_id",nullable = false)
    private String barcodeId;

    @NotNull
    @Column(name = "model")
    private String model;

    @NotNull
    @Column(name = "sell_unit")
    private String sellUnit;

    @NotNull
    @Column(name = "purchase_unit",nullable = false)
    private String purchaseUnit;

    @NotNull
    @Column(name = "existence")
    private int existence;

    //Todo: check the data type "tiny int "
    @NotNull
    @Column(name = "blocked")
    private byte blocked;

    //Todo: check the default null
    @Column(name = "picture",nullable = true)
    private String picture;

    @NotNull
    @Column(name = "discount_id")
    private String discountId;

    @Column(name = "storage_id")
    private int storageId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getRegisterDate() {
        return registerDate;
    }

    public void setRegisterDate(Date registerDate) {
        this.registerDate = registerDate;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getBarcodeId() {
        return barcodeId;
    }

    public void setBarcodeId(String barcodeId) {
        this.barcodeId = barcodeId;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getSellUnit() {
        return sellUnit;
    }

    public void setSellUnit(String sellUnit) {
        this.sellUnit = sellUnit;
    }

    public String getPurchaseUnit() {
        return purchaseUnit;
    }

    public void setPurchaseUnit(String purchaseUnit) {
        this.purchaseUnit = purchaseUnit;
    }

    public int getExistence() {
        return existence;
    }

    public void setExistence(int existence) {
        this.existence = existence;
    }

    public byte getBlocked() {
        return blocked;
    }

    public void setBlocked(byte blocked) {
        this.blocked = blocked;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDiscountId() {
        return discountId;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }

    public int getStorageId() {
        return storageId;
    }

    public void setStorageId(int storageId) {
        this.storageId = storageId;
    }
}
