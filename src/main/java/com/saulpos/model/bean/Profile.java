package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleIntegerProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Profile  extends AbstractBeanImplementation {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    @Override
    public void receiveChanges(AbstractBeanImplementation currentBean) {

    }

    @Override
    public AbstractBeanImplementation clone() {
        return null;
    }
}
