package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Profile  extends AbstractBeanImplementation<Profile> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty description = new SimpleStringProperty();

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
    public void receiveChanges(Profile currentBean) {

    }

    @Override
    public Profile clone() {
        return null;
    }
}
