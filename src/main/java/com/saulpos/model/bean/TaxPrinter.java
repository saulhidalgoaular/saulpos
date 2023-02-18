package com.saulpos.model.bean;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class TaxPrinter /*fiscal_printer*/ {

    private SimpleStringProperty identifier = new SimpleStringProperty();

    private SimpleStringProperty description = new SimpleStringProperty();

    private SimpleStringProperty printer = new SimpleStringProperty();

    //Todo check the default value
    @ColumnDefault("true")
    private SimpleBooleanProperty enabled = new SimpleBooleanProperty();

    private SimpleStringProperty report = new SimpleStringProperty();

    private SimpleStringProperty lastBill = new SimpleStringProperty();

    private SimpleStringProperty lastCreditNote = new SimpleStringProperty();

    @Id
    //Todo: is the identifier will be generated or not
    //@GeneratedValue
    public String getIdentifier(){
        return identifier.get();
    }

    public SimpleStringProperty identifierProperty(){
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier.set(identifier);
    }

    public @NotNull String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }
    public void setDescription(String description) {
        this.description.set(description);
    }

    public @NotNull String getPrinter() {
        return printer.get();
    }

    public SimpleStringProperty printerProperty() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer.set(printer);
    }

    public @NotNull boolean isEnabled() {
        return enabled.get();
    }

    public SimpleBooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public @NotNull String getReport() {
        return report.get();
    }

    public SimpleStringProperty reportProperty() {
        return report;
    }

    public void setReport(String report) {
        this.report.set(report);
    }

    public @NotNull String getLastBill() {
        return lastBill.get();
    }

    public SimpleStringProperty lastBillProperty() {
        return lastBill;
    }

    public void setLastBill(String lastBill) {
        this.lastBill.set(lastBill);
    }

    public @NotNull String getLastCreditNote() {
        return lastCreditNote.get();
    }

    public SimpleStringProperty lastCreditNoteProperty() {
        return lastCreditNote;
    }

    public void setLastCreditNote(String lastCreditNote) {
        this.lastCreditNote.set(lastCreditNote);
    }
}
