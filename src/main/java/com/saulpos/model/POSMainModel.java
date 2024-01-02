package com.saulpos.model;

import com.saulpos.model.bean.Invoice;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.beans.PropertyVetoException;

public class POSMainModel extends AbstractModel{

    private SimpleObjectProperty<Invoice> invoiceInProgressProperty = new SimpleObjectProperty<>();

    private ObservableList<Object> invoiceWaiting = FXCollections.observableArrayList();

    public POSMainModel() {
        invoiceInProgressProperty.set(new Invoice());
    }

    @Override
    public void addChangedListeners() {
        // Think where to add the bindings. If we add them in the bean, it
        // might bring inconsistencies while we load it from database.

    }

    @Override
    public void addListeners() {

    }

    @Override
    public void addDataSource() throws PropertyVetoException {

    }
}
