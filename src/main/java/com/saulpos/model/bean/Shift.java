package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalTime;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Shift extends AbstractBeanImplementation {

    private SimpleStringProperty identifier = new SimpleStringProperty();

    private SimpleStringProperty name = new SimpleStringProperty();

    private ObjectProperty<LocalTime> start = new SimpleObjectProperty<>();

    private ObjectProperty<LocalTime> end = new SimpleObjectProperty<>();


    @Id
    //@GeneratedValue
    //Todo: this primary key will be generatedValue or not
    public String getIdentifier(){
        return identifier.get();
    }

    public SimpleStringProperty identifierProperty(){
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier.set(identifier);
    }

    public String getName(){
        return name.get();
    }

    public SimpleStringProperty nameProperty(){
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public @NotNull LocalTime getStart(){
        return start.get();
    }

    public ObjectProperty<LocalTime> startProperty(){
        return start;
    }

    public void setStart(LocalTime start){
        this.start.set(start);
    }

    public @NotNull LocalTime getEnd() {
        return end.get();
    }

    public ObjectProperty<LocalTime> endProperty() {
        return end;
    }

    public void setEnd(LocalTime end) {
        this.end.set(end);
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
