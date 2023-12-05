package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.LongString;
import com.saulpos.javafxcrudgenerator.annotations.Search;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.Access;
import jakarta.persistence.AccessType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Report extends BeanImplementation<Report> {

    public enum Orientation{
        Horizontal, Vertical
    }

    @Search
    private SimpleStringProperty title = new SimpleStringProperty();

    private SimpleObjectProperty<Orientation> orientation = new SimpleObjectProperty<>();

    private SimpleIntegerProperty order = new SimpleIntegerProperty();

    @LongString(rows=10)
    private SimpleStringProperty query = new SimpleStringProperty();

    private SimpleBooleanProperty showNumbers = new SimpleBooleanProperty();

}
