package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import com.saulpos.model.menu.action.LogoutMenuAction;
import com.saulpos.model.menu.action.MenuAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class MenuModel extends AbstractBeanImplementation<MenuModel> {

    private SimpleObjectProperty<MenuType> type = new SimpleObjectProperty<>();

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty name = new SimpleStringProperty();

    private SimpleObjectProperty<MenuModel> predecessor = new SimpleObjectProperty();

    private SimpleStringProperty icon = new SimpleStringProperty();

    private SimpleStringProperty action = new SimpleStringProperty();

    public MenuModel(String name, MenuModel predecessor, String icon, String action, MenuType type) {
        this.name = new SimpleStringProperty(name);
        this.predecessor = new SimpleObjectProperty<>(predecessor);
        this.icon = new SimpleStringProperty(icon);
        this.action = new SimpleStringProperty(action);
        this.type = new SimpleObjectProperty<>(type);
    }

    private MenuAction menuAction;

    @Enumerated(EnumType.STRING)
    public MenuType getType() {
        return type.get();
    }

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id){
        this.id.set(id);
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

    @Transient
    public MenuAction getMenuAction() {
        // implement the action.
        //"com.saulpos.model.menu.LogoutMenuAction"
        return new LogoutMenuAction();
    }

    @Override
    public void receiveChanges(MenuModel currentBean) {
        // Not necessary
    }

    @Override
    public MenuModel clone() {
        // Not necessary
        return null;
    }

    public enum MenuType {
        Administrative, POS
    }
}
