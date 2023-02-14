package com.saulpos.model.bean;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "configuration")
public class Configuration {
    @Id @GeneratedValue
    @Column(name = "id")
    private int id;

    @NotNull
    @GeneratedValue
    @Column(name = "Key")
    private String key;

    @NotNull
    @Column(name = "Value")
    private String value;

    //todo check the column default value
//    @ColumnDefault(value = null)
    @Column(name = "Name")
    private String name;

    public Configuration(int id, String key, String value, String name) {
        this.id = id;
        this.key = key;
        this.value = value;
        this.name = name;
    }

    public Configuration() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
