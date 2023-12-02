package com.saulpos.model;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.model.bean.Permission;
import com.saulpos.model.bean.Profile;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.exception.SaulPosException;
import javafx.beans.property.SimpleStringProperty;
import org.hibernate.Hibernate;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

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

    public UserB checkLogin() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException, SaulPosException {
        UserB userB = new UserB();
        userB.setUserName(username.getValue());
        userB.setPassword(password.getValue());
        userB.hashPassword();
        final List list = DatabaseConnection.getInstance().listBySample(UserB.class, userB, AbstractDataProvider.SearchType.EQUAL);
        if (list.isEmpty()){
            return null;
        }
        UserB userB1 = (UserB) list.get(0);
        if (!userB1.isEnabled()){
            throw new SaulPosException("User is not enabled. Please, contact the system administrator");
        }
        Profile profile = userB1.getProfile();
        System.out.println("Permission size: " + profile);

        // Loading the permissions
        // TODO: Improve this.
        Hibernate.initialize(profile.getPermissions());
        Permission permission = new Permission();
        permission.setProfile(profile);
        final List permissionList = DatabaseConnection.getInstance().listBySample(Permission.class, permission, AbstractDataProvider.SearchType.EQUAL);
        profile.getPermissions().addAll(permissionList);

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
