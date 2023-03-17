package com.saulpos.model.bean;


import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDateTime;
// asigna
// 05.03.2023 DAMIR H. This class is checked, the create table statement matches the given through dox
@Entity
@Access(AccessType.PROPERTY)
@Table
public class Assignment extends BeanImplementation<Assignment> {
    public enum AssignmentStatus{
        Open, Closed, Cancelled
    }

    @OneToOne
    @Column(nullable = false)
    private SimpleObjectProperty<Shift> shift = new SimpleObjectProperty<>();

    @OneToOne
    private SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty<Cashier>();

    private ObjectProperty<LocalDateTime> day = new SimpleObjectProperty<>();

    private SimpleObjectProperty<AssignmentStatus> status = new SimpleObjectProperty<>();

    public Assignment() {

    }
    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    public AssignmentStatus getStatus() {
        return status.get();
    }

    public SimpleObjectProperty<AssignmentStatus> statusProperty() {
        return status;
    }

    public void setStatus(AssignmentStatus status) {
        this.status.set(status);
    }

    @OneToOne
    @JoinColumn(nullable = false)
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
    @JoinColumn(nullable = false)
    public Cashier getCashier() {
        return cashier.get();
    }

    public SimpleObjectProperty<Cashier> cashierProperty() {
        return cashier;
    }

    public void setCashier(Cashier cashier) {
        this.cashier.set(cashier);
    }

    @NotNull
    @Column(nullable = false)
    public LocalDateTime getDay() {
        return day.get();
    }

    public ObjectProperty<LocalDateTime> dayProperty() {
        return day;
    }

    public void setDay(LocalDateTime dateTime) {
        this.day.set(dateTime);
    }

}
