package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Client extends AbstractBeanImplementation<Client> {

    private SimpleStringProperty id = new SimpleStringProperty();

    private SimpleStringProperty name = new SimpleStringProperty();

    private SimpleStringProperty address = new SimpleStringProperty();

    private SimpleStringProperty phone = new SimpleStringProperty();

    public Client() {

    }

    @Id
    @GeneratedValue
    public String getId() {
        return id.get();
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getAddress() {
        return address.get();
    }

    public SimpleStringProperty addressProperty() {
        return address;
    }

    public void setAddress(String address) {
        this.address.set(address);
    }

    public String getPhone() {
        return phone.get();
    }

    public SimpleStringProperty phoneProperty() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone.set(phone);
    }

    @Override
    public void receiveChanges(Client currentBean) {

    }

    @Override
    public Client clone() {
        //Todo

        return null;
    }
}