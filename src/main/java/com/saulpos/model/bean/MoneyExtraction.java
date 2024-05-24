package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class MoneyExtraction extends BeanImplementation<BankPOS> {


    @TableViewColumn
    private final SimpleObjectProperty<UserB> user = new SimpleObjectProperty<>();

    @TableViewColumn
    private final SimpleDoubleProperty amount = new SimpleDoubleProperty();

    @TableViewColumn
    private final SimpleObjectProperty<UserB> authorizer = new SimpleObjectProperty<>();

    // Getter and Setter for user
    @ManyToOne
    public UserB getUser() {
        return user.get();
    }

    public void setUser(UserB user) {
        this.user.set(user);
    }

    public SimpleObjectProperty<UserB> userProperty() {
        return user;
    }

    // Getter and Setter for amount
    public double getAmount() {
        return amount.get();
    }

    public void setAmount(double amount) {
        this.amount.set(amount);
    }

    public SimpleDoubleProperty amountProperty() {
        return amount;
    }

    // Getter and Setter for authorizer
    @ManyToOne
    public UserB getAuthorizer() {
        return authorizer.get();
    }

    public void setAuthorizer(UserB authorizer) {
        this.authorizer.set(authorizer);
    }

    public SimpleObjectProperty<UserB> authorizerProperty() {
        return authorizer;
    }
}

