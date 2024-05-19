package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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

    public UserB getUser() {
        return user.get();
    }

    public SimpleObjectProperty<UserB> userProperty() {
        return user;
    }

    public double getAmount() {
        return amount.get();
    }

    public SimpleDoubleProperty amountProperty() {
        return amount;
    }

    public UserB getAuthorizer() {
        return authorizer.get();
    }

    public SimpleObjectProperty<UserB> authorizerProperty() {
        return authorizer;
    }
}
