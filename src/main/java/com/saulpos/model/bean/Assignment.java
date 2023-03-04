package com.saulpos.model.bean;


import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Assignment extends AbstractBeanImplementation<Assignment> {
    public enum AssignmentStatus{
        Open, Closed, Cancelled
    }

    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    @OneToOne
    private final SimpleObjectProperty<Shift> shift = new SimpleObjectProperty<>();

    @OneToOne
    private final SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty<Cashier>();

    private final ObjectProperty<LocalDate> dateTime = new SimpleObjectProperty<>();

    //Todo check the data type

    private SimpleObjectProperty<AssignmentStatus> status = new SimpleObjectProperty<>();

    public Assignment() {

    }
    @Enumerated(EnumType.STRING)
    public AssignmentStatus getStatus() {
        return status.get();
    }

    public SimpleObjectProperty<AssignmentStatus> statusProperty() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status.set(status);
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


    @Override
    public void receiveChanges(Assignment currentBean) {

    }

    @Override
    public Assignment clone() {
        //Todo
        return null;
    }
}
