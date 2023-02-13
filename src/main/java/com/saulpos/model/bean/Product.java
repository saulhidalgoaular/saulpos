package com.saulpos.model.bean;

import javafx.beans.property.SimpleIntegerProperty;

import java.util.Date;
import javax.persistence.*;

public class Product {
    @Id // TODO Confirm, I think it should be int
    private int id;
    @Column(name = "register_date")
    private Date registerDate;
}
