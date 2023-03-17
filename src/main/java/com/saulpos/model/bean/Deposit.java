package com.saulpos.model.bean;


import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Deposit extends BeanImplementation<Deposit> {
    private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private SimpleStringProperty bank =new SimpleStringProperty();
    private SimpleStringProperty number =new SimpleStringProperty();
    private SimpleDoubleProperty amount=new SimpleDoubleProperty();

    @NotNull
    public String getBank() {
        return bank.get();
    }

    public SimpleStringProperty bankProperty() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank.set(bank);
    }

    @NotNull
    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    @NotNull
    public String getNumber(){
        return number.get();
    }

    public void setNumber(String number){
        this.number.set(number);
    }

    public SimpleStringProperty numberProperty(){
        return number;
    }

    @NotNull
    public Double getAmount(){
        return amount.get();
    }

    public void setAmount(Double amount){
        this.amount.set(amount);
    }

    public SimpleDoubleProperty amountProperty(){
        return amount;
    }

}
