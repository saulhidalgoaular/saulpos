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
public class Deposit extends BeanImplementation<Deposit> {
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();
    private final SimpleStringProperty bank =new SimpleStringProperty();
    private final SimpleStringProperty number =new SimpleStringProperty();
    private final SimpleDoubleProperty amount=new SimpleDoubleProperty();

    @NotNull
    public String getBank() {
        return bank.get();
    }

    public SimpleStringProperty bankProperty() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank.set(bank);
    }

    @NotNull
    public LocalDate getDate() {
        return date.get();
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    @NotNull
    public String getNumber(){
        return number.get();
    }

    public void setNumber(String number){
        this.number.set(number);
    }

    public SimpleStringProperty numberProperty(){
        return number;
    }

    @NotNull
    public Double getAmount(){
        return amount.get();
    }

    public void setAmount(Double amount){
        this.amount.set(amount);
    }

    public SimpleDoubleProperty amountProperty(){
        return amount;
    }

}
