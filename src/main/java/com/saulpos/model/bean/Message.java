package com.saulpos.model.bean;


import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Message extends BeanImplementation<Message> {
    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty userMessage = new SimpleStringProperty();

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

    public @NotNull String getUserMessage(){
        return this.userMessage.get();
    }

    public SimpleStringProperty userMessageProperty(){
        return userMessage;
    }

    public void setUserMessage(String userMessage){
        this.userMessage.set(userMessage);
    }

    @Override
    public void receiveChanges(Message currentBean) {

    }

    @Override
    public Message clone() {
        //Todo
        return null;
    }
}
