package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Date;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Assignment extends AbstractBeanImplementation {

    private final SimpleIntegerProperty id = new SimpleIntegerProperty();

    private final SimpleStringProperty shiftId = new SimpleStringProperty();

    private final SimpleStringProperty cashierId = new SimpleStringProperty();

    //Todo check the data type
    private final ObjectProperty<Date> dateTime = new SimpleObjectProperty<>();

    //Todo check the data type
    private SimpleIntegerProperty status=new SimpleIntegerProperty();

    public Assignment() {

    }

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

    public String getShiftId() {
        return shiftId.get();
    }

    public void setShiftId(String shiftId) {
        this.shiftId.set(shiftId);
    }

    public @NotNull SimpleStringProperty shiftIdProperty() {
        return shiftId;
    }

    public String getCashierId() {
        return cashierId.get();
    }

    public void setCashierId(String cashierId) {
        this.cashierId.set(cashierId);
    }

    public @NotNull SimpleStringProperty cashierIdProperty() {
        return cashierId;
    }

    @NotNull
    public Date getDateTime() {
        return this.dateTime.get();
    }

    public void setDateTime(Date dateTime) {
        this.dateTime.set(dateTime);
    }

    public @NotNull ObjectProperty<Date> dateTimeProperty() {
        return dateTime;
    }

    public @NotNull int getStatus() {
        return status.get();
    }

    public void setStatus(int status) {
        this.status.set(status);
    }

    public SimpleIntegerProperty statusProperty() {
        return id;
    }

    @Override
    public void receiveChanges(AbstractBeanImplementation currentBean) {
        //Todo
    }

    @Override
    public AbstractBeanImplementation clone() {
        //Todo
        return null;
    }
}
