package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class BankSalePoint extends AbstractBeanImplementation {

    private SimpleStringProperty id = new SimpleStringProperty();

    private SimpleStringProperty description = new SimpleStringProperty();

    private SimpleStringProperty batch = new SimpleStringProperty();

    //tipo
    //Todo: check the type of the attribute
    private SimpleStringProperty guy = new SimpleStringProperty();

    //identificador pos
    private SimpleStringProperty identifierPos = new SimpleStringProperty();

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

    public String getIdentifierPos() {
        return identifierPos.get();
    }

    public SimpleStringProperty identifierPosProperty() {
        return identifierPos;
    }

    public void setIdentifierPos(String identifierPos) {
        this.identifierPos.set(identifierPos);
    }

    public String getGuy() {
        return guy.get();
    }

    public SimpleStringProperty guyProperty() {
        return guy;
    }

    public void setGuy(String guy) {
        this.guy.set(guy);
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
