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
package com.saulpos.model;

import com.saulpos.javafxcrudgenerator.model.Function;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.Profile;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.exception.SaulPosException;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.hibernate.Hibernate;

import java.beans.PropertyVetoException;
import java.util.List;

public class LoginModel extends AbstractModel{

    private final SimpleStringProperty username = new SimpleStringProperty("");

    private final SimpleStringProperty password = new SimpleStringProperty("");

    private final SimpleObjectProperty<MenuModel.MenuType> systemType = new SimpleObjectProperty<>(MenuModel.MenuType.Administrative);

    private final ListProperty<MenuModel.MenuType> allSystemType = new SimpleListProperty<>(
            FXCollections.observableArrayList(MenuModel.MenuType.Administrative, MenuModel.MenuType.POS)
    );

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

    public MenuModel.MenuType getSystemType() {
        return systemType.get();
    }

    public SimpleObjectProperty<MenuModel.MenuType> systemTypeProperty() {
        return systemType;
    }

    public void setSystemType(MenuModel.MenuType systemType) {
        this.systemType.set(systemType);
    }

    public ObservableList<MenuModel.MenuType> getAllSystemType() {
        return allSystemType.get();
    }

    public ListProperty<MenuModel.MenuType> allSystemTypeProperty() {
        return allSystemType;
    }

    public void setAllSystemType(ObservableList<MenuModel.MenuType> allSystemType) {
        this.allSystemType.set(allSystemType);
    }

    public UserB checkLogin() throws Exception {
        UserB userB = new UserB();
        userB.setUserName(username.getValue());
        userB.setPassword(password.getValue());
        userB.hashPassword();
        final List list = DatabaseConnection.getInstance().listBySample(UserB.class, userB, AbstractDataProvider.SearchType.EQUAL, new Function() {
            @Override
            public Object[] run(Object[] objects) throws Exception {
                List users = (List) objects[0];

                if (users.isEmpty()){
                    return null;
                }

                UserB userB1 = (UserB) users.get(0);
                Hibernate.initialize(userB1.getProfile().getPermissions());
                return null;
            }
        });
        if (list.isEmpty()){
            return null;
        }
        UserB userB1 = (UserB) list.get(0);
        if (!userB1.isEnabled()){
            throw new SaulPosException("User is not enabled. Please, contact the system administrator");
        }
        Profile profile = userB1.getProfile();
        System.out.println("Permission size: " + profile);

        return userB1;
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
