package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.*;

import java.time.LocalDate;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Discount extends BeanImplementation<Discount> {

    private SimpleStringProperty description=new SimpleStringProperty();

    private ObjectProperty<LocalDate> startingDate=new SimpleObjectProperty<>();

    private ObjectProperty<LocalDate> endingDate = new SimpleObjectProperty<>();

    private SimpleDoubleProperty percentage=new SimpleDoubleProperty();

    public Discount() {

    }

    public String getDescription(){
        return description.get();
    }
    public void setDescription(String description){
        this.description.set(description);
    }
    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public LocalDate getStartingDate(){
        return startingDate.get();
    }

    public void setStartingDate(LocalDate startingDate){
        this.startingDate.set(startingDate);
    }
    public ObjectProperty<LocalDate> startingDateProperty() {
        return startingDate;
    }
    public LocalDate getEndingDate(){
        return endingDate.get();
    }

    public void setEndingDate(LocalDate endingDate){
        this.endingDate.set(endingDate);
    }
    public ObjectProperty<LocalDate> endingDateProperty() {
        return endingDate;
    }

    public Double getPercentage(){
        return this.percentage.get();
    }
    public void setPercentage(Double percentage){
        this.percentage.set(percentage);
    }
    public SimpleDoubleProperty percentageProperty(){
        return percentage;
    }

}
