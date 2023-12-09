/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.Ignore;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class ReportColumn extends BeanImplementation<ReportColumn> implements Comparable<ReportColumn> {


    public enum Type{
        StringType, IntegerType, BigDecimalType, DateType, DateTypeToFractionType, DateYearType, DateDayType, DateMonthType
    }

    private final SimpleStringProperty name = new SimpleStringProperty();

    private final SimpleStringProperty sqlName = new SimpleStringProperty();

    private final SimpleObjectProperty<Type> columnType = new SimpleObjectProperty<>();

    @Ignore
    private final SimpleObjectProperty<Report> report = new SimpleObjectProperty<>();

    private SimpleIntegerProperty columnOrder = new SimpleIntegerProperty();

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Type getColumnType() {
        return columnType.get();
    }

    public SimpleObjectProperty<Type> columnTypeProperty() {
        return columnType;
    }

    public void setColumnType(Type columnType) {
        this.columnType.set(columnType);
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "report_id")
    public Report getReport() {
        return report.get();
    }

    public SimpleObjectProperty<Report> reportProperty() {
        return report;
    }

    public void setReport(Report report) {
        this.report.set(report);
    }

    public int getColumnOrder() {
        return columnOrder.get();
    }

    public SimpleIntegerProperty columnOrderProperty() {
        return columnOrder;
    }

    public void setColumnOrder(int columnOrder) {
        this.columnOrder.set(columnOrder);
    }

    public String getSqlName() {
        return sqlName.get();
    }

    public SimpleStringProperty sqlNameProperty() {
        return sqlName;
    }

    public void setSqlName(String sqlName) {
        this.sqlName.set(sqlName);
    }

    @Override
    public int compareTo(ReportColumn o) {
        return this.getColumnOrder() - o.getColumnOrder();
    }
}
