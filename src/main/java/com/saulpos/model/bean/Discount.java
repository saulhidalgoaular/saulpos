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

import com.saulpos.javafxcrudgenerator.annotations.DisplayOrder;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.time.LocalDate;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Discount extends BeanImplementation<Discount> {

    @DisplayOrder(orderValue = 1)
    private final SimpleStringProperty description=new SimpleStringProperty();
    @DisplayOrder(orderValue = 2)
    private final ObjectProperty<LocalDate> startingDate=new SimpleObjectProperty<>();
    @DisplayOrder(orderValue = 3)
    private final ObjectProperty<LocalDate> endingDate = new SimpleObjectProperty<>();
    @DisplayOrder(orderValue = 4)
    private final SimpleDoubleProperty percentage=new SimpleDoubleProperty();
    @DisplayOrder(orderValue = 5)
    private final SimpleObjectProperty<Product> product = new SimpleObjectProperty<>();

    public Discount() {

    }

    public String getDescription(){
        return description.get();
    }
    public void setDescription(String description){
        this.description.set(description);
    }
    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public LocalDate getStartingDate(){
        return startingDate.get();
    }

    public void setStartingDate(LocalDate startingDate){
        this.startingDate.set(startingDate);
    }
    public ObjectProperty<LocalDate> startingDateProperty() {
        return startingDate;
    }
    public LocalDate getEndingDate(){
        return endingDate.get();
    }

    public void setEndingDate(LocalDate endingDate){
        this.endingDate.set(endingDate);
    }
    public ObjectProperty<LocalDate> endingDateProperty() {
        return endingDate;
    }

    public Double getPercentage(){
        return this.percentage.get();
    }
    public void setPercentage(Double percentage){
        this.percentage.set(percentage);
    }
    public SimpleDoubleProperty percentageProperty(){
        return percentage;
    }

    @NotNull
    @OneToOne(mappedBy = "discount")
    public Product getProduct() {
        return product.get();
    }

    public SimpleObjectProperty<Product> productProperty() {
        return product;
    }

    public void setProduct(Product product) {
        this.product.set(product);
    }

    @Override
    public String toString() {
        if(getBeanStatus().equals(BeanStatus.Deleted)){
            return "0.0%";
        }
       return String.valueOf(percentage.get() + "%");
    }
}
