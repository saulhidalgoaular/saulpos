package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Deposit extends AbstractBeanImplementation<Deposit> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    // TODO Verify
    private SimpleStringProperty number =new SimpleStringProperty();

    private SimpleDoubleProperty amount=new SimpleDoubleProperty();

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

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

    public String getNumber(){
        return number.get();
    }
    public void setNumber(String number){
        this.number.set(number);
    }
    public @NotNull SimpleStringProperty numberProperty(){
        return number;
    }
    public Double getAmount(){
        return amount.get();
    }
    public void setAmount(Double amount){
        this.amount.set(amount);
    }
    public @NotNull SimpleDoubleProperty amountProperty(){
        return amount;
    }

    @Override
    public void receiveChanges(Deposit currentBean) {

    }

    @Override
    public Deposit clone() {
        //Todo
        return null;
    }
}
