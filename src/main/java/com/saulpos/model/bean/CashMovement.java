package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class CashMovement extends AbstractBeanImplementation {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    //identificador_punto_de_venta
    private SimpleStringProperty pointOfSaleIdentifier = new SimpleStringProperty();

    //fecha
    private ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();

    //monto
    @ColumnDefault("0.00")
    private SimpleDoubleProperty amount = new SimpleDoubleProperty();


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

    public @NotNull String getPointOfSaleIdentifier() {
        return pointOfSaleIdentifier.get();
    }

    public SimpleStringProperty pointOfSaleIdentifierProperty() {
        return pointOfSaleIdentifier;
    }

    public void setPointOfSaleIdentifier(String pointOfSaleIdentifier) {
        this.pointOfSaleIdentifier.set(pointOfSaleIdentifier);
    }

    public @NotNull LocalDateTime getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDateTime> dateProperty() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date.set(date);
    }

    public @NotNull Double getAmount() {
        return amount.get();
    }

    public SimpleDoubleProperty amountProperty() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount.set(amount);
    }

    @Override
    public void receiveChanges(AbstractBeanImplementation currentBean) {
        //Todo
    }

    @Override
    public AbstractBeanImplementation clone() {
        //Todo

        return null;
    }
}
