package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class FiscalPrinter extends AbstractBeanImplementation<FiscalPrinter> {

    @Max(10)
    private SimpleStringProperty identifier = new SimpleStringProperty();

    @Max(200)
    private SimpleStringProperty description = new SimpleStringProperty();

    @ColumnDefault("True")
    private SimpleBooleanProperty enabled = new SimpleBooleanProperty();

    @Max(8)
    private SimpleStringProperty reportZ = new SimpleStringProperty();

    @Max(14)
    private SimpleStringProperty lastBill = new SimpleStringProperty();

    @Max(14)
    private SimpleStringProperty lastCreditNote = new SimpleStringProperty();


    @Id
    @GeneratedValue
    public String getIdentifier() {
        return identifier.get();
    }

    public SimpleStringProperty identifierProperty() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier.set(identifier);
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

    public @NotNull boolean isEnabled() {
        return enabled.get();
    }

    public SimpleBooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public @NotNull String getReportZ() {
        return reportZ.get();
    }

    public SimpleStringProperty reportZProperty() {
        return reportZ;
    }

    public void setReportZ(String reportZ) {
        this.reportZ.set(reportZ);
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

    @Override
    public void receiveChanges(FiscalPrinter fiscalPrinter) {

    }

    @Override
    public FiscalPrinter clone() {
        //Todo
        return null;
    }
}