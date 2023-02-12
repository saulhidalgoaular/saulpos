package com.saulpos.model.bean;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity(name="Storage") // TODO
@Table(name="Storage") // TODO Research
public class Storage implements AbstractBean {

    @Id // TODO Confirm
    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty description = new SimpleStringProperty();

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
}
