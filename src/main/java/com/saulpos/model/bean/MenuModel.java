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

import com.saulpos.model.dao.BeanImplementation;
import com.saulpos.model.menu.action.MenuAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class MenuModel extends BeanImplementation<MenuModel> implements Comparable<MenuModel> {

    private SimpleObjectProperty<MenuType> type = new SimpleObjectProperty<>();

    private SimpleStringProperty name = new SimpleStringProperty();

    private SimpleObjectProperty<MenuModel> predecessor = new SimpleObjectProperty();

    private SimpleStringProperty icon = new SimpleStringProperty();

    private SimpleStringProperty action = new SimpleStringProperty();

    private SimpleIntegerProperty menuOrder = new SimpleIntegerProperty();

    public MenuModel(String name, MenuModel predecessor, String icon, String action, MenuType type, int order) {
        this.name = new SimpleStringProperty(name);
        this.predecessor = new SimpleObjectProperty<>(predecessor);
        this.icon = new SimpleStringProperty(icon);
        this.action = new SimpleStringProperty(action);
        this.type = new SimpleObjectProperty<>(type);
        this.menuOrder = new SimpleIntegerProperty(order);
    }

    public MenuModel() {
    }

    @Enumerated(EnumType.STRING)
    public MenuType getType() {
        return type.get();
    }

    public @NotNull String getName(){
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @OneToOne
    public MenuModel getPredecessor() {
        return predecessor.get();
    }

    public void setPredecessor(MenuModel predecessor) {
        this.predecessor.set(predecessor);
    }

    public SimpleObjectProperty<MenuModel> predecessorProperty() {
        return predecessor;
    }

    public String getIcon(){
        return icon.get();
    }

    public SimpleStringProperty iconProperty() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon.set(icon);
    }

    public @NotNull String getAction() {
        return action.get();
    }

    public void setAction(String action) {
        this.action.set(action);
    }

    public SimpleStringProperty actionProperty() {
        return action;
    }

    public void setType(MenuType type) {
        this.type.set(type);
    }

    public SimpleObjectProperty<MenuType> typeProperty() {
        return type;
    }

    public int getMenuOrder() {
        return menuOrder.get();
    }

    public SimpleIntegerProperty menuOrderProperty() {
        return menuOrder;
    }

    public void setMenuOrder(int menuOrder) {
        this.menuOrder.set(menuOrder);
    }

    @Transient
    public MenuAction getMenuAction() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        String actionClassName = "com.saulpos.model.menu.action." + getAction();
        Class<?> dogClass = Class.forName(actionClassName); // convert string classname to class
        return (MenuAction) dogClass.newInstance(); // invoke empty constructor
    }

    @Override
    public int compareTo(MenuModel o) {
        return getMenuOrder() - o.getMenuOrder();
    }

    public enum MenuType {
        Administrative, POS
    }

    @Override
    public String toString() {
        return name.getValue();
    }
}
