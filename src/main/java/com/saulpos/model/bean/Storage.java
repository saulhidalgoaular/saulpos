package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Table(name="storage")
@Access(AccessType.PROPERTY)
public class Storage extends AbstractBeanImplementation<Storage> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    @Column(name="description")
    private SimpleStringProperty description = new SimpleStringProperty();


    @Id
    @GeneratedValue
    @Column(name = "id")
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }


    @Override
    public void receiveChanges(Storage currentBean) {

    }

    @Override
    public Storage clone() {
        return null;
    }
}
