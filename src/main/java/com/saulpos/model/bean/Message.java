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
    private SimpleStringProperty userMessage = new SimpleStringProperty();

    @NotNull
    public String getUserMessage(){
        return this.userMessage.get();
    }

    public SimpleStringProperty userMessageProperty(){
        return userMessage;
    }

    public void setUserMessage(String userMessage){
        this.userMessage.set(userMessage);
    }

}
