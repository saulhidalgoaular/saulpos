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
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDate;
// pagos de punto de venta
// 05.03.2023 DAMIR H. This class is checked, the create table statement matches the given through dox
@Entity
@Access(AccessType.PROPERTY)
@Table
public class BankPOSMachine extends BeanImplementation<BankPOSMachine> {
    public enum Medium{
        Debit, Credit, AmericanExpress
    }
    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<LocalDate>();
    private final SimpleObjectProperty<BankPOS> bankPontOfSale = new SimpleObjectProperty<BankPOS>();
    private final SimpleObjectProperty<BankPOSMachine.Medium> medium = new SimpleObjectProperty<BankPOSMachine.Medium>();
    private final SimpleFloatProperty declaration = new SimpleFloatProperty();
    private final SimpleFloatProperty actualAmount = new SimpleFloatProperty();
    private final SimpleStringProperty batch = new SimpleStringProperty();

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

    @NotNull
    @Column(nullable = false)
    public float getActualAmount() {
        return actualAmount.get();
    }

    public SimpleFloatProperty actualAmountProperty() {
        return actualAmount;
    }

    public void setActualAmount(float actualAmount) {
        this.actualAmount.set(actualAmount);
    }

    @NotNull
    @Column(nullable = false)
    @ColumnDefault("0.00")
    public float getDeclaration() {
        return declaration.get();
    }

    public SimpleFloatProperty declarationProperty() {
        return declaration;
    }

    public void setDeclaration(float declaration) {
        this.declaration.set(declaration);
    }

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public Medium getMedium() {
        return medium.get();
    }

    public SimpleObjectProperty<Medium> mediumProperty() {
        return medium;
    }

    public void setMedium(Medium medium) {
        this.medium.set(medium);
    }

    @OneToOne
    @JoinColumn(nullable = false)
    public BankPOS getBankPontOfSale() {
        return bankPontOfSale.get();
    }

    public SimpleObjectProperty<BankPOS> bankPontOfSaleProperty() {
        return bankPontOfSale;
    }

    public void setBankPontOfSale(BankPOS bankPontOfSale) {
        this.bankPontOfSale.set(bankPontOfSale);
    }

    @NotNull
    @Column(nullable = false)
    public LocalDate getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date.set(date);
    }

}
