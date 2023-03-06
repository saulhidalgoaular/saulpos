package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalTime;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Shift extends BeanImplementation<Shift> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty name = new SimpleStringProperty();

    private ObjectProperty<LocalTime> start = new SimpleObjectProperty<>();

    private ObjectProperty<LocalTime> end = new SimpleObjectProperty<>();

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public @NotNull LocalTime getStart() {
        return start.get();
    }

    public ObjectProperty<LocalTime> startProperty() {
        return start;
    }

    public void setStart(LocalTime start) {
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
    public void receiveChanges(Shift currentBean) {

    }

    @Override
    public Shift clone() {
        //Todo
        return null;
    }
}
