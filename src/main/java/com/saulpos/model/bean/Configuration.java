package com.saulpos.model.bean;


import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Configuration extends BeanImplementation<Configuration> {
    private String key;
    private String value;
    private String name;

    public Configuration() {

    }

    @NotNull
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @NotNull
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
