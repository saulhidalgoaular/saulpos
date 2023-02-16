package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Message extends AbstractBeanImplementation{
    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    //mensaje
    //todo: rename this attribute to be different from the entity name
    private SimpleStringProperty message= new SimpleStringProperty();

    @Id
    @GeneratedValue
    public int getId(){
        return id.get();
    }

    public SimpleIntegerProperty idProperty(){
        return id;
    }

    public void setId(int id){
        this.id.set(id);
    }

    public @NotNull String getMessage(){
        return this.message.get();
    }

    public SimpleStringProperty messageProperty(){
        return message;
    }

    public void setMessage(String message){
        this.message.set(message);
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
