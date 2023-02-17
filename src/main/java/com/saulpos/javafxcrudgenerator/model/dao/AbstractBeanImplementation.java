package com.saulpos.javafxcrudgenerator.model.dao;

import com.saulpos.javafxcrudgenerator.DatabaseConnection;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;

public abstract class AbstractBeanImplementation<I extends AbstractBeanImplementation> implements AbstractBean<I>{

    private static final long serialVersionUID = 1L;

    @Override
    public Integer save() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        return DatabaseConnection.getInstance().createEntry(this);
    }

    @Override
    public void update() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        DatabaseConnection.getInstance().update(this);
    }

    @Override
    public void saveOrUpdate() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        DatabaseConnection.getInstance().saveOrUpdate(this);
    }

    @Override
    public void delete() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        DatabaseConnection.getInstance().delete(this);
    }
}
