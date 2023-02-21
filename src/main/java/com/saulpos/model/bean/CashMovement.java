package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class CashMovement extends AbstractBeanImplementation<CashMovement> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    //identificador_punto_de_venta
    @OneToOne
    private SimpleObjectProperty<Cashier> cashier = new SimpleObjectProperty();

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

    @OneToOne
    public Cashier getCashier() {
        return cashier.get();
    }

    public SimpleObjectProperty<Cashier> cashierProperty() {
        return cashier;
    }

    public void setCashier(Cashier cashier) {
        this.cashier.set(cashier);
    }

    public LocalDateTime getDate() {
        return date.get();
    }

    public ObjectProperty<LocalDateTime> dateProperty() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date.set(date);
    }

    public double getAmount() {
        return amount.get();
    }

    public SimpleDoubleProperty amountProperty() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    @Override
    public void receiveChanges(CashMovement currentBean) {

    }

    @Override
    public CashMovement clone() {
        //Todo

        return null;
    }
}
