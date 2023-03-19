package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
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

    private SimpleStringProperty description = new SimpleStringProperty();
    private SimpleStringProperty batch = new SimpleStringProperty();
    private SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty();
    private SimpleObjectProperty<BankPOS.POSType> POSType = new SimpleObjectProperty<BankPOS.POSType>();

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

    @OneToOne
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

}
