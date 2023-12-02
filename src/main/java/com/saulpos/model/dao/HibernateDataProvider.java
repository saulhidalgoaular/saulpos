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
    public List getAllItems(Class aClass) {
        return getAllItems(aClass, null, SearchType.LIKE);
    }

    @Override
    public List getAllItems(Class aClass, AbstractBean abstractBean, SearchType type)  {
        try {
            return DatabaseConnection.getInstance().listBySample(aClass, abstractBean, type);
        } catch (PropertyVetoException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
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
