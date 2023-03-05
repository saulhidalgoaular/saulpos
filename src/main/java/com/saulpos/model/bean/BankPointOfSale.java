package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.hibernate.annotations.ColumnDefault;

// 05.03.2023 DAMIR H. This class is checked, the create table statement matches the given through dox
@Entity
@Access(AccessType.PROPERTY)
@Table
public class BankPointOfSale extends AbstractBeanImplementation<BankPointOfSale> {
    public enum Type{
        Debit, Credit, AmericanExpress, All
    }

    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleStringProperty description = new SimpleStringProperty();
    private SimpleStringProperty batch = new SimpleStringProperty();
    private SimpleStringProperty POSIdentificator = new SimpleStringProperty();
    private SimpleObjectProperty<BankPointOfSale.Type> type = new SimpleObjectProperty<BankPointOfSale.Type>();

    @NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public Type getType() {
        return type.get();
    }

    public SimpleObjectProperty<Type> typeProperty() {
        return type;
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    @NotNull
    @Column(nullable = false)
    public String getPOSIdentificator() {
        return POSIdentificator.get();
    }

    public SimpleStringProperty POSIdentificatorProperty() {
        return POSIdentificator;
    }

    public void setPOSIdentificator(String POSIdentificator) {
        this.POSIdentificator.set(POSIdentificator);
    }

    @NotNull
    @Column(nullable = false)
    public String getBatch() {
        return batch.get();
    }

    public SimpleStringProperty batchProperty() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch.set(batch);
    }

    @ColumnDefault("")
    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    @Override
    public BankPointOfSale clone() {
        //Todo
        return null;
    }
    @Override
    public void receiveChanges(BankPointOfSale currentBean) {

    }
}
