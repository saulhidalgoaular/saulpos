package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class ProductMovement extends AbstractBeanImplementation<ProductMovement> {

    private SimpleStringProperty id = new SimpleStringProperty();

    private ObjectProperty<LocalDate> date = new SimpleObjectProperty<>();

    private SimpleStringProperty description = new SimpleStringProperty();

    private SimpleStringProperty code = new SimpleStringProperty();

    private SimpleObjectProperty<Storage> store = new SimpleObjectProperty();

    // TODO We need a set of Product Movement details
    // Please, take a look at this.
    // https://www.baeldung.com/hibernate-one-to-many

    @Id
    @GeneratedValue
    public String getId(){
        return id.get();
    }

    public SimpleStringProperty idProperty(){
        return id;
    }

    public void setId(String id){
        this.id.set(id);
    }

    public @NotNull LocalDate getDate(){
        return date.get();
    }

    public ObjectProperty<LocalDate> dateProperty(){
        return date;
    }

    public void setDate(LocalDate date){
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

    @Override
    public void receiveChanges(ProductMovement currentBean) {

    }

    @Override
    public ProductMovement clone() {
        //Todo
        return null;
    }
}
