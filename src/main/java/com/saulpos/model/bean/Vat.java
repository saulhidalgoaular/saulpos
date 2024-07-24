package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javafx.beans.property.SimpleDoubleProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Vat extends BeanImplementation<Product> {

    public Vat() {
    }

    private final SimpleStringProperty description = new SimpleStringProperty();

    private final SimpleDoubleProperty percentage = new SimpleDoubleProperty();
    private final SimpleObjectProperty<Product> product = new SimpleObjectProperty<>();

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

    @NotNull
    @OneToOne(mappedBy = "vat")
    public Product getProduct() {
        return product.get();
    }

    public SimpleObjectProperty<Product> productProperty() {
        return product;
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    @Override
    public String toString() {
        if(getBeanStatus().equals(BeanStatus.Deleted)){
            return "0.0%";
        }
        return String.valueOf(percentage.get() + "%");
    }
}
