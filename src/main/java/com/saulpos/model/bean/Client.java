package com.saulpos.model.bean;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnDefault;

import javax.annotation.processing.Generated;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;

//todo check the primray key of the table

@Entity
@Table(name = "client")
public class Client {
    @Id @GeneratedValue
    @Column(name = "id")
    private String id;

    //todo check default null value
//    @ColumnDefault(value = null)
    @Column(name = "name")
    private String name;

    //todo check default null value
    @Column(name = "address")
    private String address;

    @Column(name = "phone")
    private String phone;

    public Client(String id, String name, String address, String phone) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phone = phone;
    }

    public Client() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
