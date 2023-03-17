package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import com.saulpos.model.bean.Storage;
import javafx.beans.property.Property;
import javafx.beans.value.ObservableValue;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;

public abstract class BeanImplementation<I extends BeanImplementation> extends AbstractBeanImplementation<I> {
    private static final long serialVersionUID = 1L;

    @Override
    public void save() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        super.save();
        DatabaseConnection.getInstance().createEntry(this);
    }

    @Override
    public void update() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        super.update();
        DatabaseConnection.getInstance().update(this);
    }

    @Override
    public void saveOrUpdate() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        super.saveOrUpdate();
        DatabaseConnection.getInstance().saveOrUpdate(this);
    }

    @Override
    public void delete() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        DatabaseConnection.getInstance().delete(this);
    }

    @Override
    public I clone(){
        Class<? extends BeanImplementation> aClass = this.getClass();
        try {
            I newObject = (I) aClass.getConstructor().newInstance();
            newObject.receiveChanges(this);
            return newObject;
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean previouslySaved() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        return false;
    }

    public void receiveChanges(I currentBean) {
        Class<? extends BeanImplementation> aClass = this.getClass();
        for (Field field : aClass.getDeclaredFields()){
            try {
                String getter = DaoUtils.getGetter(field.getName(), field.getType());
                String setter = DaoUtils.getSetter(field.getName());

                if (!ObservableValue.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                Method getValue = field.getType().getMethod("getValue");

                Method setterMethod = null;
                for (Method method : aClass.getDeclaredMethods()){
                    if (method.getName().equals(setter)){
                        setterMethod = method;
                    }
                }
                if (setterMethod == null){
                    continue;
                }
                Method getterMethod = aClass.getDeclaredMethod(getter);
                setterMethod.invoke(this, getterMethod.invoke(currentBean));
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
