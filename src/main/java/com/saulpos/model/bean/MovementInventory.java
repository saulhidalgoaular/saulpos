package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
//Todo check the primary is generated or not
public class MovementInventory extends AbstractBeanImplementation {

    private SimpleStringProperty identifier= new SimpleStringProperty();

    private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    private SimpleStringProperty description = new SimpleStringProperty();

    private SimpleStringProperty code = new SimpleStringProperty();

    private SimpleStringProperty store = new SimpleStringProperty();


    public @Id String getIdentifier(){
        return identifier.get();
    }

    public SimpleStringProperty identifierProperty(){
        return identifier;
    }

    public void setIdentifier(String identifier){
        this.identifier.set(identifier);
    }

    public @NotNull LocalDate getDate(){
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty(){
        return date;
    }

    public void setDate(LocalDate date){
        this.date.set(date);
    }

    public @NotNull String getDescription(){
        return description.get();
    }

    public SimpleStringProperty descriptionProperty(){
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public @NotNull String getCode(){
        return code.get();
    }

    public SimpleStringProperty codeProperty(){
        return code;
    }

    public void setCode(String code){
        this.code.set(code);
    }

    public @NotNull String getStore(){
        return store.get();
    }

    public SimpleStringProperty storeProperty() {
        return store;
    }

    public void setStore(String store) {
        this.store.set(store);
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
