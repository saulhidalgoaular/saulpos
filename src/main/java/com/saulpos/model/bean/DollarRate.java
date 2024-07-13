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
    private final SimpleDoubleProperty localCurrencyRate = new SimpleDoubleProperty();
    @TableViewColumn
    @DisplayOrder(orderValue = 3)
    private final SimpleBooleanProperty activated = new SimpleBooleanProperty(false);

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

    @Column(columnDefinition="tinyint(1) default 0", nullable = false)
    public boolean isActivated() {
        return activated.get();
    }

    public SimpleBooleanProperty activatedProperty() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated.set(activated);
    }
}
