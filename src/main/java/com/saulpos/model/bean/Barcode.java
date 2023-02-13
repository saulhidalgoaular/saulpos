package com.saulpos.model.bean;

//ToDO check the attributes again
public class Barcode {
    private String productId;
    private int id;

    public Barcode(String productId, int id) {
        this.productId = productId;
        this.id = id;
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