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
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

// punto de venta de banco
// 05.03.2023 DAMIR H. This class is checked, the create table statement matches the given through dox
@Entity
@Access(AccessType.PROPERTY)
@Table
public class BankPOS extends BeanImplementation<BankPOS> {
    public enum POSType {
        Debit, Credit, AmericanExpress, All
    }

    @TableViewColumn
    private final SimpleStringProperty description = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleStringProperty batch = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty();
    @TableViewColumn
    private final SimpleObjectProperty<BankPOS.POSType> POSType = new SimpleObjectProperty<BankPOS.POSType>();

    private final SimpleDoubleProperty minimumAmount = new SimpleDoubleProperty();

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public BankPOS.POSType getPOSType() {
        return POSType.get();
    }

    public SimpleObjectProperty<BankPOS.POSType> POSTypeProperty() {
        return POSType;
    }

    public void setPOSType(BankPOS.POSType POSType) {
        this.POSType.set(POSType);
    }

    @ManyToOne
    @NotNull
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
    public String getBatch() {
        return batch.get();
    }

    public SimpleStringProperty batchProperty() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch.set(batch);
    }


    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public double getMinimumAmount() {
        return minimumAmount.get();
    }

    public SimpleDoubleProperty minimumAmountProperty() {
        return minimumAmount;
    }

    public void setMinimumAmount(double minimumAmount) {
        this.minimumAmount.set(minimumAmount);
    }
}
