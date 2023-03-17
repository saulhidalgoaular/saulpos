package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Set;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Profile  extends BeanImplementation<Profile> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleStringProperty description = new SimpleStringProperty();
    private ObjectProperty<Set<Permission>> permissions = new SimpleObjectProperty<>();

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

    @OneToMany
    public Set<Permission> getPermissions() {
        return permissions.get();
    }

    public ObjectProperty<Set<Permission>> permissionsProperty() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions.set(permissions);
    }

}
