package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Expense extends BeanImplementation<Expense> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private SimpleStringProperty concept = new SimpleStringProperty();
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

    @NotNull
    public LocalDate getDate(){
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty(){
        return date;
    }

    public void setDate(LocalDate date){
        this.date.set(date);
    }

    @NotNull
    public String getConcept(){
        return this.concept.get();
    }

    public SimpleStringProperty conceptProperty(){
        return this.concept;
    }

    public void setConcept(String concept){
        this.concept.set(concept);
    }

    @NotNull
    public Double getAmount(){
        return amount.get();
    }

    public SimpleDoubleProperty amountProperty(){
        return amount;
    }

    public void setAmount(Double amount){
        this.amount.set(amount);
    }

    @NotNull
    public String getDescription(){
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
