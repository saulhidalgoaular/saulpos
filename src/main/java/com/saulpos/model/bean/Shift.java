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

import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;
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

    public LocalTime getShiftStart() {
        return shiftStart.get();
    }

    public ObjectProperty<LocalTime> shiftStartProperty() {
        return shiftStart;
    }

    public void setShiftStart(LocalTime shiftStart) {
        this.shiftStart.set(shiftStart);
    }

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
