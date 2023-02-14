package com.saulpos.model.bean;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

//TODO CHECK THE CONSTRAINTS primay key
@Entity
@Table(name = "deposit")
public class Deposit {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;

    @NotNull
    @Column(name = "data")
    private Date date;

    @NotNull
    @Column(name = "bank")
    private String bank;

    @NotNull
    @Column(name = "numero")
    private String number;

    @NotNull
    @Column(name = "amount")
    private double amount;

    public Deposit(int id, @NotNull Date date, @NotNull String bank, @NotNull String number, @NotNull double amount) {
        this.id = id;
        this.date = date;
        this.bank = bank;
        this.number = number;
        this.amount = amount;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
