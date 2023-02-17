package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class UserType extends AbstractBeanImplementation<UserType> {

    private SimpleStringProperty id = new SimpleStringProperty();

    private SimpleStringProperty description = new SimpleStringProperty();

    @Id
    @GeneratedValue
    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public @NotNull String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    @Override
    public void receiveChanges(UserType currentBean) {

    }

    @Override
    public UserType clone() {
        return null;
    }
}
