/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

//
@Entity
@Access(AccessType.PROPERTY)
@Table
public class Cashier extends BeanImplementation<Cashier> {
    @TableViewColumn
    private final SimpleStringProperty description = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleStringProperty physicalName = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleStringProperty printer = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleBooleanProperty enabled = new SimpleBooleanProperty();
    @TableViewColumn
    private final SimpleStringProperty ZReport = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleObjectProperty<Invoice> lastInvoice = new SimpleObjectProperty();
    @TableViewColumn(minWidth = 120, prefWidth = 150)
    private final SimpleObjectProperty<CreditNote> lastCreditNote = new SimpleObjectProperty();
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

    public String getPhysicalName() {
        return physicalName.get();
    }

    public SimpleStringProperty physicalNameProperty() {
        return physicalName;
    }

    public void setPhysicalName(String newPhysicalName){
        physicalName.setValue(newPhysicalName);
    }

    public Cashier() {
    }

    @Override
    public String toString() {
        return description.getValue();
    }
}
