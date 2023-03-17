package com.saulpos.model;

import javafx.beans.property.SimpleStringProperty;

import java.beans.PropertyVetoException;

public class LoginModel extends AbstractModel{

    private SimpleStringProperty username = new SimpleStringProperty("");

    private SimpleStringProperty password = new SimpleStringProperty("");

    public String getUsername() {
        return username.get();
    }

    public SimpleStringProperty usernameProperty() {
        return username;
    }

    public void setUsername(String username) {
        this.username.set(username);
    }

    public String getPassword() {
        return password.get();
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public boolean checkLogin(){

        return true;
    }

    @Override
    public void addChangedListeners() {

    }

    @Override
    public void addListeners() {

    }

    @Override
    public void addDataSource() throws PropertyVetoException {

    }
}
