package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class BankPOS extends AbstractBeanImplementation<BankPOS> {

    private SimpleStringProperty id = new SimpleStringProperty();

    private SimpleStringProperty description = new SimpleStringProperty();

    private SimpleStringProperty batch = new SimpleStringProperty();

    private SimpleStringProperty type = new SimpleStringProperty();

    @Id
    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
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

    public @NotNull String getBatch() {
        return batch.get();
    }

    public SimpleStringProperty batchProperty() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch.set(batch);
    }

    public String getType() {
        return type.get();
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    public void setType(String type) {
        this.type.set(type);
    }

    @Override
    public void receiveChanges(BankPOS currentBean) {

    }

    @Override
    public BankPOS clone() {
        //Todo
        return null;
    }
}
