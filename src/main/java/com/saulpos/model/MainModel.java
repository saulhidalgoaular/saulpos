package com.saulpos.model;

import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.UserB;

import java.beans.PropertyVetoException;
import java.util.ArrayList;

public class MainModel extends AbstractModel{

    private UserB userB;


    public MainModel(UserB userB) {
        this.userB = userB;
    }

    public UserB getUserB() {
        return userB;
    }

    public void setUserB(UserB userB) {
        this.userB = userB;
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
