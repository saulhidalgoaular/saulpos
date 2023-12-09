package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.Ignore;
import com.saulpos.javafxcrudgenerator.annotations.LongString;
import com.saulpos.javafxcrudgenerator.annotations.Search;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.Set;
import java.util.TreeSet;

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

    @LongString(rows=10)
    private SimpleStringProperty query = new SimpleStringProperty();

    private SimpleBooleanProperty showNumbers = new SimpleBooleanProperty();

    @Ignore
    private final SimpleObjectProperty<Set<ReportColumn>> columns = new SimpleObjectProperty<>(new TreeSet<>());

    public Report() {
    }

    public String getTitle() {
        return title.get();
    }

    public SimpleStringProperty titleProperty() {
        return title;
    }

    public void setTitle(String title) {
        this.title.set(title);
    }

    public Orientation getOrientation() {
        return orientation.get();
    }

    public SimpleObjectProperty<Orientation> orientationProperty() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        this.orientation.set(orientation);
    }

    public String getQuery() {
        return query.get();
    }

    public SimpleStringProperty queryProperty() {
        return query;
    }

    public void setQuery(String query) {
        this.query.set(query);
    }

    public boolean isShowNumbers() {
        return showNumbers.get();
    }

    public SimpleBooleanProperty showNumbersProperty() {
        return showNumbers;
    }

    public void setShowNumbers(boolean showNumbers) {
        this.showNumbers.set(showNumbers);
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "report")
    public Set<ReportColumn> getColumns() {
        return columns.get();
    }

    public SimpleObjectProperty<Set<ReportColumn>> columnsProperty() {
        return columns;
    }

    public void setColumns(Set<ReportColumn> columns) {
        this.columns.set(columns);
    }
}
