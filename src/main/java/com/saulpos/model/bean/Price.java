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

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Price extends BeanImplementation<Price> {
    private final SimpleObjectProperty<Product> product = new SimpleObjectProperty<>();
    private final SimpleDoubleProperty price = new SimpleDoubleProperty();
    private final ObjectProperty<LocalDate> fromDate = new SimpleObjectProperty<>();

    private final ObjectProperty<LocalDate> toDate = new SimpleObjectProperty<>();

    @NotNull
    @OneToOne
    public Product getProduct() {
        return product.get();
    }

    public SimpleObjectProperty<Product> productProperty() {
        return product;
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    @NotNull
    public Double getPrice() {
        return price.get();
    }

    public void setPrice(Double price) {
        this.price.set(price);
    }

    public SimpleDoubleProperty priceProperty() {
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    @NotNull
    public LocalDate getFromDate() {
        return fromDate.get();
    }

    public ObjectProperty<LocalDate> fromDateProperty() {
        return fromDate;
    }

    public void setFromDate(LocalDate fromDate) {
        this.fromDate.set(fromDate);
    }

    public LocalDate getToDate() {
        return toDate.get();
    }

    public ObjectProperty<LocalDate> toDateProperty() {
        return toDate;
    }

    public void setToDate(LocalDate toDate) {
        this.toDate.set(toDate);
    }

    @Override
    public String toString() {
        return price.toString();
    }
}
