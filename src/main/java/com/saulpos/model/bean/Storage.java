package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Table(name="storage")
@Access(AccessType.PROPERTY)
public class Storage extends BeanImplementation<Storage> {

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

    @NotNull
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
