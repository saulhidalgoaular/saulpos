package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleIntegerProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Unit extends AbstractBeanImplementation<Unit> {

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
    public void receiveChanges(Unit currentBean) {

    }

    @Override
    public Unit clone() {
        return null;
    }
}
