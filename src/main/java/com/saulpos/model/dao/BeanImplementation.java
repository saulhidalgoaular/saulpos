package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;

public abstract class BeanImplementation<I extends BeanImplementation> extends AbstractBeanImplementation<I> {
    private static final long serialVersionUID = 1L;

    @Override
    public void save() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        DatabaseConnection.getInstance().createEntry(this);
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

    @Override
    public abstract I clone();

    @Override
    public boolean previouslySaved() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        return false;
    }
}
