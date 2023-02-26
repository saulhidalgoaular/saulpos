package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.annotations.Ignore;
import com.saulpos.javafxcrudgenerator.annotations.Search;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;


import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.ParameterExpression;
import jakarta.persistence.criteria.Root;


public class HibernateDataProvider implements AbstractDataProvider {

    final TreeSet<Class> registeredClasses = new TreeSet<>();

    @Override
    public List getAllItems(Class aClass) {
        return getAllItems(aClass, null);
    }

    @Override
    public List getAllItems(Class aClass, AbstractBean abstractBean) {
        final Field[] allFields = aClass.getDeclaredFields();

        //Restrictions
        //List<>
        for (Field field : allFields) {
            if (field.isAnnotationPresent(Search.class)) {

            }
        }

        return null;
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
