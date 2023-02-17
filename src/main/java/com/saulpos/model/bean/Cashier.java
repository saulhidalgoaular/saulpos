package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Cashier extends AbstractBeanImplementation<Cashier> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty description = new SimpleStringProperty();

    private SimpleStringProperty printerID = new SimpleStringProperty();

    private SimpleBooleanProperty enabled = new SimpleBooleanProperty();

    private SimpleStringProperty ZReport = new SimpleStringProperty();

    private SimpleStringProperty lastInvoiceID = new SimpleStringProperty();

    private SimpleStringProperty lastCreditNoteID = new SimpleStringProperty();

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
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

    public String getPrinterID() {
        return printerID.get();
    }

    public SimpleStringProperty printerIDProperty() {
        return printerID;
    }

    public void setPrinterID(String printerID) {
        this.printerID.set(printerID);
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    public SimpleBooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public String getZReport() {
        return ZReport.get();
    }

    public SimpleStringProperty ZReportProperty() {
        return ZReport;
    }

    public void setZReport(String ZReport) {
        this.ZReport.set(ZReport);
    }

    public String getLastInvoiceID() {
        return lastInvoiceID.get();
    }

    public SimpleStringProperty lastInvoiceIDProperty() {
        return lastInvoiceID;
    }

    public void setLastInvoiceID(String lastInvoiceID) {
        this.lastInvoiceID.set(lastInvoiceID);
    }

    public String getLastCreditNoteID() {
        return lastCreditNoteID.get();
    }

    public SimpleStringProperty lastCreditNoteIDProperty() {
        return lastCreditNoteID;
    }

    public void setLastCreditNoteID(String lastCreditNoteID) {
        this.lastCreditNoteID.set(lastCreditNoteID);
    }

    public Cashier() {
    }



    @Override
    public void receiveChanges(Cashier currentBean) {

    }

    @Override
    public Cashier clone() {
        return null;
    }
}
