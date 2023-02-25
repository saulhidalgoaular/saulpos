package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import com.saulpos.model.menu.MenuAction;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class MenuModel extends AbstractBeanImplementation<MenuModel> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty name = new SimpleStringProperty();

    private SimpleObjectProperty<MenuModel> predecessor = new SimpleObjectProperty();

    private SimpleStringProperty icon = new SimpleStringProperty();

    private SimpleStringProperty action = new SimpleStringProperty();

    private SimpleBooleanProperty administrative = new SimpleBooleanProperty();

    private SimpleBooleanProperty pointOfSale = new SimpleBooleanProperty();

    private MenuAction menuAction;

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

    public @NotNull Boolean isAdministrative() {
        return administrative.get();
    }

    public SimpleBooleanProperty administrativeProperty() {
        return administrative;
    }

    public void setAdministrative(boolean administrative) {
        this.administrative.set(administrative);
    }

    public boolean isPointOfSale() {
        return pointOfSale.get();
    }

    public SimpleBooleanProperty pointOfSaleProperty() {
        return pointOfSale;
    }

    public void setPointOfSale(boolean pointOfSale) {
        this.pointOfSale.set(pointOfSale);
    }

    @Transient
    public MenuAction getMenuAction() {
        // implement the action.
        return new MenuAction() {
            @Override
            public Object run() {
                System.out.println("Hello World menu");
                return null;
            }
        };
    }

    @Override
    public void receiveChanges(MenuModel currentBean) {

    }

    @Override
    public MenuModel clone() {
        //Todo
        return null;
    }
}
