package com.saulpos.model.bean;

import javafx.beans.property.SimpleIntegerProperty;

public class Product {
    @Id // TODO Confirm, I think it should be int
    private int id = new SimpleIntegerProperty();
    private Date registerDate;
}
