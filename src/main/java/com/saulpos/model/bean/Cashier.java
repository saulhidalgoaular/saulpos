package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

//
@Entity
@Access(AccessType.PROPERTY)
@Table
public class Cashier extends BeanImplementation<Cashier> {
    @TableViewColumn
    private SimpleStringProperty description = new SimpleStringProperty();
    @TableViewColumn
    private SimpleStringProperty printer = new SimpleStringProperty();
    @TableViewColumn
    private SimpleBooleanProperty enabled = new SimpleBooleanProperty();
    @TableViewColumn
    private SimpleStringProperty ZReport = new SimpleStringProperty();
    @TableViewColumn
    private SimpleObjectProperty<Invoice> lastInvoice = new SimpleObjectProperty();
    @TableViewColumn(minWidth = 120, prefWidth = 150)
    private SimpleObjectProperty<CreditNote> lastCreditNote = new SimpleObjectProperty();
    @NotNull
    @Column(nullable = false)
    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
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

    public String getPrinter() {
        return printer.get();
    }

    public SimpleStringProperty printerProperty() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer.set(printer);
    }

    @OneToOne
    public Invoice getLastInvoice() {
        return lastInvoice.get();
    }

    public SimpleObjectProperty<Invoice> lastInvoiceProperty() {
        return lastInvoice;
    }

    public void setLastInvoice(Invoice lastInvoice) {
        this.lastInvoice.set(lastInvoice);
    }

    @OneToOne
    public CreditNote getLastCreditNote() {
        return lastCreditNote.get();
    }

    public SimpleObjectProperty<CreditNote> lastCreditNoteProperty() {
        return lastCreditNote;
    }

    public void setLastCreditNote(CreditNote lastCreditNote) {
        this.lastCreditNote.set(lastCreditNote);
    }

    public Cashier() {
    }

    @Override
    public String toString() {
        return description.getValue();
    }
}
