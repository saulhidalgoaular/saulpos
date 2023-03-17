package com.saulpos.model.dao;

import javafx.beans.property.SimpleBooleanProperty;

public class DaoUtils {

    public static String getGetter(final String fieldName, final Class type){
        return (SimpleBooleanProperty.class.equals(type) ? "is" : "get") + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }

    public static String getSetter(final String fieldName){
        return "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
    }


}
