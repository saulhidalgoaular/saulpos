package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.DisplayOrder;
import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class DollarRate extends BeanImplementation<DollarRate> {

    public DollarRate() {
    }

    @TableViewColumn
    @DisplayOrder(orderValue = 1)
    private final SimpleStringProperty localCurrencyName = new SimpleStringProperty();
    @TableViewColumn
    @DisplayOrder(orderValue = 2)
    private final SimpleDoubleProperty exchangeRatePerDollar = new SimpleDoubleProperty();
    @TableViewColumn
    @DisplayOrder(orderValue = 3)
    private final SimpleBooleanProperty enabled = new SimpleBooleanProperty(false);

    public String getLocalCurrencyName() {
        return localCurrencyName.get();
    }

    public SimpleStringProperty localCurrencyNameProperty() {
        return localCurrencyName;
    }

    public void setLocalCurrencyName(String localCurrencyName) {
        this.localCurrencyName.set(localCurrencyName);
    }

    public double getExchangeRatePerDollar() {
        return exchangeRatePerDollar.get();
    }

    public SimpleDoubleProperty exchangeRatePerDollarProperty() {
        return exchangeRatePerDollar;
    }

    public void setExchangeRatePerDollar(double exchangeRatePerDollar) {
        this.exchangeRatePerDollar.set(exchangeRatePerDollar);
    }

    @Column(nullable = false)
    public boolean isEnabled() {
        return enabled.get();
    }

    public SimpleBooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }
}
