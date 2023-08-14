package com.saulpos.model.bean;


import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleStringProperty;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Configuration extends BeanImplementation<Configuration> {
    @TableViewColumn
    private SimpleStringProperty keyConfig = new SimpleStringProperty();
    @TableViewColumn
    private SimpleStringProperty valueConfig = new SimpleStringProperty();
    @TableViewColumn
    private SimpleStringProperty name = new SimpleStringProperty();

    public Configuration() {

    }

    public String getKeyConfig() {
        return keyConfig.get();
    }

    public SimpleStringProperty keyConfigProperty() {
        return keyConfig;
    }

    public void setKeyConfig(String keyConfig) {
        this.keyConfig.set(keyConfig);
    }

    public String getValueConfig() {
        return valueConfig.get();
    }

    public SimpleStringProperty valueConfigProperty() {
        return valueConfig;
    }

    public void setValueConfig(String valueConfig) {
        this.valueConfig.set(valueConfig);
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
}
