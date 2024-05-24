/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.*;
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
    @DisplayOrder(orderValue = 0)
    @TableViewColumn
    private final SimpleStringProperty userName = new SimpleStringProperty();
    @Password
    @DisplayOrder(orderValue = 1)
    private final SimpleStringProperty password = new SimpleStringProperty();
    @Ignore
    private final SimpleStringProperty passwordHashed = new SimpleStringProperty();

    @DisplayOrder(orderValue = 2)
    @TableViewColumn
    private final SimpleStringProperty name = new SimpleStringProperty();

    @DisplayOrder(orderValue = 3)
    @TableViewColumn
    private final SimpleStringProperty lastname = new SimpleStringProperty();

    @DisplayOrder(orderValue = 4)
    @TableViewColumn
    @ManyToOne
    private final SimpleObjectProperty<Profile> profile = new SimpleObjectProperty<>();

    @DisplayOrder(orderValue = 5)
    private final SimpleBooleanProperty enabled = new SimpleBooleanProperty();

    @DisplayOrder(orderValue = 6)
    @TableViewColumn(minWidth = 160, prefWidth = 170)
    private final SimpleBooleanProperty shouldChangePassword = new SimpleBooleanProperty();

    @DisplayOrder(orderValue = 7)
    @TableViewColumn(minWidth = 150, prefWidth = 170)
    private final SimpleBooleanProperty canChangePassword = new SimpleBooleanProperty();

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

    @OneToOne(cascade = CascadeType.ALL)
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
        if (password.getValue() == null) {
            return;
        }
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
