package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.util.Date;
//Todo check the bank attribute
//Todo check the PRIMARY KEY (fecha,banco,numero)
@Entity
@Access(AccessType.PROPERTY)
@Table
public class Deposit extends AbstractBeanImplementation {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private ObjectProperty<Date> date = new SimpleObjectProperty<>();

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

    public  @NotNull Date getDate() {
        return date.get();
    }

    public void setDate(Date date) {
        this.date.set(date);
    }

    public @NotNull ObjectProperty<Date> dateProperty() {
        return date;
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
    public void receiveChanges(AbstractBeanImplementation currentBean) {
        //Todo
    }

    @Override
    public AbstractBeanImplementation clone() {
        //Todo
        return null;
    }
}
