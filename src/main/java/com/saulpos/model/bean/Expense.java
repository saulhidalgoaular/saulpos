package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Expense extends AbstractBeanImplementation<Expense> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    //fecha
    private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    //concepto
    private SimpleStringProperty concept = new SimpleStringProperty();

    //monto
    private SimpleDoubleProperty amount = new SimpleDoubleProperty();

    private SimpleStringProperty description = new SimpleStringProperty();

    @Id
    @GeneratedValue
    public int getId(){
        return id.get();
    }

    public SimpleIntegerProperty idProperty(){
        return id;
    }

    public void setId(int id){
        this.id.set(id);
    }

    public @NotNull LocalDate getDate(){
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty(){
        return date;
    }

    public void setDate(LocalDate date){
        this.date.set(date);
    }

    public @NotNull String getConcept(){
        return this.concept.get();
    }

    public SimpleStringProperty conceptProperty(){
        return this.concept;
    }

    public void setConcept(String concept){
        this.concept.set(concept);
    }

    public @NotNull Double getAmount(){
        return amount.get();
    }

    public SimpleDoubleProperty amountProperty(){
        return amount;
    }

    public void setAmount(Double amount){
        this.amount.set(amount);
    }

    public @NotNull String getDescription(){
        return description.get();
    }

    public SimpleStringProperty descriptionProperty(){
        return description;
    }

    public void setDescription(String description){
        this.description.set(description);
    }


    @Override
    public void receiveChanges(Expense currentBean) {

    }

    @Override
    public Expense clone() {
        //Todo
        return null;
    }
}
