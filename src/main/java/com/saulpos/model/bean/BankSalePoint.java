package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class BankSalePoint extends AbstractBeanImplementation {
    @Max(30)
    private SimpleStringProperty id = new SimpleStringProperty();

    @Max(200)
    private SimpleStringProperty description = new SimpleStringProperty();

    @Max(15)
    private SimpleStringProperty batch = new SimpleStringProperty();

    @Max(10)
    //identificador_pos
    private SimpleStringProperty identifierPos= new SimpleStringProperty();

    //tipo
    private SimpleStringProperty guy = new SimpleStringProperty();

    @Id
    @GeneratedValue
    public String getId(){
        return id.get();
    }

    public SimpleStringProperty idProperty(){
        return id;
    }

    public void setId(String id){
        this.id.set(id);
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

    public @NotNull String getBatch() {
        return batch.get();
    }

    public SimpleStringProperty batchProperty() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch.set(batch);
    }

    public @NotNull String getIdentifierPos() {
        return identifierPos.get();
    }

    public SimpleStringProperty identifierPosProperty() {
        return identifierPos;
    }

    public void setIdentifierPos(String identifierPos) {
        this.identifierPos.set(identifierPos);
    }

    public @NotNull String getGuy() {
        return guy.get();
    }

    public SimpleStringProperty guyProperty() {
        return guy;
    }

    public void setGuy(String guy) {
        this.guy.set(guy);
    }

    @Override
    public void receiveChanges(AbstractBean currentBean) {
        //Todo
    }

    @Override
    public AbstractBeanImplementation clone() {
        //Todo
        return null;
    }
}
