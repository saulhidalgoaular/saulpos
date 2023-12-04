/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    private final SimpleStringProperty keyConfig = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleStringProperty valueConfig = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleStringProperty name = new SimpleStringProperty();

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
