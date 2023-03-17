package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class ProductMovement extends BeanImplementation<ProductMovement> {

    private ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();
    private SimpleStringProperty description = new SimpleStringProperty();
    private SimpleStringProperty code = new SimpleStringProperty();
    private SimpleObjectProperty<Storage> store = new SimpleObjectProperty();
    private ObjectProperty<Set<ProductMovementDetail>> price = new SimpleObjectProperty<>();

    public @NotNull LocalDateTime getDate(){
        return date.get();
    }

    public ObjectProperty<LocalDateTime> dateProperty(){
        return date;
    }

    public void setDate(LocalDateTime date){
        this.date.set(date);
    }

    public @NotNull String getDescription(){
        return description.get();
    }

    public SimpleStringProperty descriptionProperty(){
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public @NotNull String getCode(){
        return code.get();
    }

    public SimpleStringProperty codeProperty(){
        return code;
    }

    public void setCode(String code){
        this.code.set(code);
    }

    @OneToOne
    public Storage getStore() {
        return store.get();
    }

    public SimpleObjectProperty<Storage> storeProperty() {
        return store;
    }

    public void setStore(Storage store) {
        this.store.set(store);
    }

    @OneToMany
    public Set<ProductMovementDetail> getPrice() {
        return price.get();
    }

    public ObjectProperty<Set<ProductMovementDetail>> priceProperty() {
        return price;
    }

    public void setPrice(Set<ProductMovementDetail> price) {
        this.price.set(price);
    }

}
