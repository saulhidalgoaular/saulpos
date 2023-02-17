package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


@Entity
@Table(name = "configuration")
public class Configuration extends AbstractBeanImplementation<Configuration> {
    @Id @GeneratedValue
    @Column(name = "id")
    private int id;

    @NotNull
    private String key;

    @Column(name = "Value")
    private String value;

    private String name;

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

    @Override
    public void receiveChanges(Configuration currentBean) {

    }

    @Override
    public Configuration clone() {
        return null;
    }
}
