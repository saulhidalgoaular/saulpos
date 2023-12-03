package com.saulpos.model;

import com.saulpos.javafxcrudgenerator.model.Function;
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
