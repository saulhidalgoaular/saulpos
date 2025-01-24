package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.Ignore;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.List;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Vat extends BeanImplementation<Product> {

    public Vat() {
    }

    private final SimpleStringProperty description = new SimpleStringProperty();

    private final SimpleDoubleProperty percentage = new SimpleDoubleProperty();

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public double getPercentage() {
        return percentage.get();
    }

    public SimpleDoubleProperty percentageProperty() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage.set(percentage);
    }


    @Override
    public String toString() {
        if(getBeanStatus().equals(BeanStatus.Deleted)){
            return "0.0%";
        }
        return String.valueOf(percentage.get() + "%");
    }
}
