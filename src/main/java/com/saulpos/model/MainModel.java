package com.saulpos.model;

import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.UserB;

import java.beans.PropertyVetoException;
import java.util.ArrayList;

public class MainModel extends AbstractModel{

    private UserB userB;

    private ArrayList<MenuModel> menuModel;

    public MainModel(UserB userB) {
        this.userB = userB;
    }

    public ArrayList<MenuModel> getMenuModel() {
        return menuModel;
    }

    public void setMenuModel(ArrayList<MenuModel> menuModel) {
        this.menuModel = menuModel;
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
