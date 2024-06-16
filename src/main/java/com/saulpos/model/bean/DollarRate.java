package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class DollarRate extends BeanImplementation<DollarRate> {

    public DollarRate() {
    }

    @TableViewColumn
    private final SimpleStringProperty localCurrencyName = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleDoubleProperty localCurrencyRate = new SimpleDoubleProperty();

    public String getLocalCurrencyName() {
        return localCurrencyName.get();
    }

    public SimpleStringProperty localCurrencyNameProperty() {
        return localCurrencyName;
    }

    public void setLocalCurrencyName(String localCurrencyName) {
        this.localCurrencyName.set(localCurrencyName);
    }

    public double getLocalCurrencyRate() {
        return localCurrencyRate.get();
    }

    public SimpleDoubleProperty localCurrencyRateProperty() {
        return localCurrencyRate;
    }

    public void setLocalCurrencyRate(double localCurrencyRate) {
        this.localCurrencyRate.set(localCurrencyRate);
    }

}
