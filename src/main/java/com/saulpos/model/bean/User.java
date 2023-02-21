package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class User extends AbstractBeanImplementation<User> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty password = new SimpleStringProperty();

    private SimpleStringProperty name = new SimpleStringProperty();

    private SimpleStringProperty lastname = new SimpleStringProperty();

    private SimpleObjectProperty<Profile> profile = new SimpleObjectProperty<>();

    private SimpleBooleanProperty enabled = new SimpleBooleanProperty();

    private SimpleBooleanProperty shouldChangePassword = new SimpleBooleanProperty();

    private SimpleBooleanProperty canChangePassword = new SimpleBooleanProperty();


    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
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

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getLastname() {
        return lastname.get();
    }

    public SimpleStringProperty lastnameProperty() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname.set(lastname);
    }

    @OneToOne
    public Profile getProfile() {
        return profile.get();
    }

    public SimpleObjectProperty<Profile> profileProperty() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile.set(profile);
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

    public boolean isShouldChangePassword() {
        return shouldChangePassword.get();
    }

    public SimpleBooleanProperty shouldChangePasswordProperty() {
        return shouldChangePassword;
    }

    public void setShouldChangePassword(boolean shouldChangePassword) {
        this.shouldChangePassword.set(shouldChangePassword);
    }

    public boolean isCanChangePassword() {
        return canChangePassword.get();
    }

    public SimpleBooleanProperty canChangePasswordProperty() {
        return canChangePassword;
    }

    public void setCanChangePassword(boolean canChangePassword) {
        this.canChangePassword.set(canChangePassword);
    }


    @Override
    public void receiveChanges(User currentBean) {

    }

    @Override
    public User clone() {
        return null;
    }
}
