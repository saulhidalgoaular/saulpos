/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.annotations.Readonly;
import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

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
    @TableViewColumn
    private final SimpleObjectProperty<Shift> shift = new SimpleObjectProperty<>();

    @OneToOne
    @TableViewColumn
    private final SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty<Cashier>();

    @Readonly
    @TableViewColumn(minWidth = 120, prefWidth = 150)
    private final ObjectProperty<LocalDate> assignmentDay = new SimpleObjectProperty<>(LocalDate.now());

    @TableViewColumn(minWidth = 130, prefWidth = 160)
    private final SimpleObjectProperty<AssignmentStatus> assignmentStatus = new SimpleObjectProperty<>();

    public Assignment() {

    }

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    public AssignmentStatus getAssignmentStatus() {
        return assignmentStatus.get();
    }

    public SimpleObjectProperty<AssignmentStatus> assignmentStatusProperty() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(AssignmentStatus assignmentStatus) {
        this.assignmentStatus.set(assignmentStatus);
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
    public LocalDate getAssignmentDay() {
        return assignmentDay.get();
    }

    public ObjectProperty<LocalDate> assignmentDayProperty() {
        return assignmentDay;
    }

    public void setAssignmentDay(LocalDate date) {
        this.assignmentDay.set(date);
    }

}
