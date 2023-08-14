package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalTime;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Shift extends BeanImplementation<Shift> {

    @TableViewColumn
    private SimpleStringProperty shiftName = new SimpleStringProperty();

    @TableViewColumn
    private ObjectProperty<LocalTime> shiftStart = new SimpleObjectProperty<>();

    @TableViewColumn
    private ObjectProperty<LocalTime> shiftEnd = new SimpleObjectProperty<>();

    public String getShiftName() {
        return shiftName.get();
    }

    public SimpleStringProperty shiftNameProperty() {
        return shiftName;
    }

    public void setShiftName(String shiftName) {
        this.shiftName.set(shiftName);
    }

    @NotNull
    public LocalTime getShiftStart() {
        return shiftStart.get();
    }

    public ObjectProperty<LocalTime> shiftStartProperty() {
        return shiftStart;
    }

    public void setShiftStart(LocalTime shiftStart) {
        this.shiftStart.set(shiftStart);
    }

    @NotNull
    public LocalTime getShiftEnd() {
        return shiftEnd.get();
    }

    public ObjectProperty<LocalTime> shiftEndProperty() {
        return shiftEnd;
    }

    public void setShiftEnd(LocalTime shiftEnd) {
        this.shiftEnd.set(shiftEnd);
    }

    @Override
    public String toString() {
        return shiftName.getValue();
    }
}
