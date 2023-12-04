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

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Expense extends BeanImplementation<Expense> {

    private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private SimpleStringProperty concept = new SimpleStringProperty();
    private SimpleDoubleProperty amount = new SimpleDoubleProperty();
    private SimpleStringProperty description = new SimpleStringProperty();

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

}
