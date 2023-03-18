package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.Search;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.codec.digest.DigestUtils;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class UserB extends BeanImplementation<UserB> {

    @Search
    private SimpleStringProperty userName = new SimpleStringProperty();
    private SimpleStringProperty password = new SimpleStringProperty();
    private SimpleStringProperty passwordHashed = new SimpleStringProperty();
    private SimpleStringProperty name = new SimpleStringProperty();
    private SimpleStringProperty lastname = new SimpleStringProperty();
    private SimpleObjectProperty<Profile> profile = new SimpleObjectProperty<>();
    private SimpleBooleanProperty enabled = new SimpleBooleanProperty();
    private SimpleBooleanProperty shouldChangePassword = new SimpleBooleanProperty();
    private SimpleBooleanProperty canChangePassword = new SimpleBooleanProperty();

    public String getPasswordHashed() {
        return passwordHashed.get();
    }

    public void setPasswordHashed(String passwordHashed) {
        this.passwordHashed.set(passwordHashed);
    }

    public SimpleStringProperty passwordHashedProperty() {
        return passwordHashed;
    }

    @NotNull
    public String getUserName() {
        return userName.get();
    }

    public SimpleStringProperty userNameProperty() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    @NotNull
    public String getPassword() {
        return password.get();
    }

    public SimpleStringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    @NotNull
    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @NotNull
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
    @NotNull
    public Profile getProfile() {
        return profile.get();
    }

    public SimpleObjectProperty<Profile> profileProperty() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile.set(profile);
    }

    @ColumnDefault("true")
    public boolean isEnabled() {
        return enabled.get();
    }

    public SimpleBooleanProperty enabledProperty() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    @ColumnDefault("false")
    public boolean isShouldChangePassword() {
        return shouldChangePassword.get();
    }

    public SimpleBooleanProperty shouldChangePasswordProperty() {
        return shouldChangePassword;
    }

    public void setShouldChangePassword(boolean shouldChangePassword) {
        this.shouldChangePassword.set(shouldChangePassword);
    }

    @ColumnDefault("false")
    public boolean isCanChangePassword() {
        return canChangePassword.get();
    }

    public SimpleBooleanProperty canChangePasswordProperty() {
        return canChangePassword;
    }

    public void setCanChangePassword(boolean canChangePassword) {
        this.canChangePassword.set(canChangePassword);
    }

    public void hashPassword() {
        passwordHashed.set(
                DigestUtils.sha1Hex(password.get())
        );
        password.set(null);
    }

    @Override
    public String toString() {
        return "UserB{" +
                "id=" + getId() +
                ", userName=" + userName +
                ", password=" + password +
                ", name=" + name +
                ", lastname=" + lastname +
                ", profile=" + profile +
                ", enabled=" + enabled +
                ", shouldChangePassword=" + shouldChangePassword +
                ", canChangePassword=" + canChangePassword +
                '}';
    }
}
