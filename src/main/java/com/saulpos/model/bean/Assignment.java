package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Assignment extends AbstractBeanImplementation<Assignment> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private final SimpleObjectProperty<Shift> shift = new SimpleObjectProperty<>();

    private final SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty();

    //Todo check the data type
    private final ObjectProperty<LocalDate> dateTime = new SimpleObjectProperty<>();

    //Todo check the data type
    private SimpleIntegerProperty status=new SimpleIntegerProperty();

    public Assignment() {

    }

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

    @OneToOne
    public Shift getShift() {
        return shift.get();
    }

    public SimpleObjectProperty<Shift> shiftProperty() {
        return shift;
    }

    public void setShift(Shift shift) {
        this.shift.set(shift);
    }

    @OneToOne
    public Cashier getCashier() {
        return cashier.get();
    }

    public SimpleObjectProperty<Cashier> cashierProperty() {
        return cashier;
    }

    public void setCashier(Cashier cashier) {
        this.cashier.set(cashier);
    }

    public LocalDate getDateTime() {
        return dateTime.get();
    }

    public ObjectProperty<LocalDate> dateTimeProperty() {
        return dateTime;
    }

    public void setDateTime(LocalDate dateTime) {
        this.dateTime.set(dateTime);
    }

    public int getStatus() {
        return status.get();
    }

    public SimpleIntegerProperty statusProperty() {
        return status;
    }

    public void setStatus(int status) {
        this.status.set(status);
    }

    @Override
    public void receiveChanges(Assignment currentBean) {

    }

    @Override
    public Assignment clone() {
        //Todo
        return null;
    }
}
