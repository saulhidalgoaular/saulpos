package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Node extends AbstractBeanImplementation<Node> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty name = new SimpleStringProperty();

    private SimpleStringProperty predecessor = new SimpleStringProperty();

    private SimpleStringProperty icon = new SimpleStringProperty();

    private SimpleStringProperty function = new SimpleStringProperty();

    private SimpleBooleanProperty administrative = new SimpleBooleanProperty();

    private SimpleBooleanProperty pointOfSale = new SimpleBooleanProperty();

    @Id
    @GeneratedValue
    public int getId(){
        return id.get();
    }

    public SimpleIntegerProperty idProperty(){
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

    public String getPredecessor(){
        return predecessor.get();
    }

    public SimpleStringProperty predecessorProperty() {
        return predecessor;
    }

    public void setPredecessor(String predecessor) {
        this.predecessor.set(predecessor);
    }

    public String getIcon(){
        return icon.get();
    }

    public SimpleStringProperty iconProperty(){
        return icon;
    }

    public void setIcon(String icon){
        this.icon.set(icon);
    }

    public @NotNull String getFunction(){
        return function.get();
    }

    public SimpleStringProperty functionProperty(){
        return function;
    }

    public void setFunction(String function){
        this.function.set(function);
    }

    public @NotNull Boolean isAdministrative(){
        return administrative.get();
    }

    public SimpleBooleanProperty administrativeProperty(){
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

    @Override
    public void receiveChanges(Node currentBean) {

    }

    @Override
    public Node clone() {
        //Todo
        return null;
    }
}
