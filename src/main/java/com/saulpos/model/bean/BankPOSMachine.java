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
    private ObjectProperty<LocalDate> date = new SimpleObjectProperty<LocalDate>();
    private SimpleObjectProperty<BankPointOfSale> bankPontOfSale = new SimpleObjectProperty<BankPointOfSale>();
    private SimpleObjectProperty<BankPOSMachine.Medium> medium = new SimpleObjectProperty<BankPOSMachine.Medium>();
    private SimpleFloatProperty declaration = new SimpleFloatProperty();
    private SimpleFloatProperty actualAmount = new SimpleFloatProperty();
    private SimpleStringProperty batch = new SimpleStringProperty();

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
    public BankPointOfSale getBankPontOfSale() {
        return bankPontOfSale.get();
    }

    public SimpleObjectProperty<BankPointOfSale> bankPontOfSaleProperty() {
        return bankPontOfSale;
    }

    public void setBankPontOfSale(BankPointOfSale bankPontOfSale) {
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
