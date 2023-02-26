package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;


public class HibernateDataProvider implements AbstractDataProvider {

    final HashSet<Class> registeredClasses = new HashSet<>();

    @Override
    public List getAllItems(Class aClass) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        return getAllItems(aClass, null);
    }

    @Override
    public List getAllItems(Class aClass, AbstractBean abstractBean) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        return DatabaseConnection.getInstance().listBySample(abstractBean);
    }

    @Override
    public boolean isRegisteredClass(Class aClass) {
        return registeredClasses.contains(aClass);
    }

    @Override
    public void registerClass(Class aClass) {
        registeredClasses.add(aClass);
    }
}
