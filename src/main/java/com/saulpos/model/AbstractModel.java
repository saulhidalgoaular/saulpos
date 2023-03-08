package com.saulpos.model;

import java.beans.PropertyVetoException;
import java.util.ResourceBundle;

/**
 * Created by Saul on 10/23/2016.
 */
public abstract class AbstractModel {
    ResourceBundle language;

    public AbstractModel(){
        //language = ResourceBundle.getBundle("Language");
        //initialize();
    }

    protected void initialize() throws PropertyVetoException {
        addDataSource();
        addListeners();
        addChangedListeners();
    }

    public abstract void addChangedListeners();
    public abstract void addListeners();
    public abstract void addDataSource() throws PropertyVetoException;

    public ResourceBundle getLanguage() {
        return language;
    }
}