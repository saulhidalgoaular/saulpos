package com.saulpos.model.bean;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.persistence.*;

@Entity
@Table(name="storage")
public class Storage/* implements AbstractBean*/ {

    @Id @GeneratedValue
    @Column(name = "id")
    private int id;

    @Column(name="description")
    private SimpleStringProperty description = new SimpleStringProperty();

    public int getId() {
        return id;
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
