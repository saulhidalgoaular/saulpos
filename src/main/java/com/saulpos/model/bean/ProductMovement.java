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
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class ProductMovement extends BeanImplementation<ProductMovement> {

    private final ObjectProperty<LocalDateTime> date = new SimpleObjectProperty<>();
    private final SimpleStringProperty description = new SimpleStringProperty();
    private final SimpleStringProperty code = new SimpleStringProperty();
    private final SimpleObjectProperty<Storage> store = new SimpleObjectProperty();
    private final ObjectProperty<Set<ProductMovementDetail>> price = new SimpleObjectProperty<>();

    public @NotNull LocalDateTime getDate(){
        return date.get();
    }

    public ObjectProperty<LocalDateTime> dateProperty(){
        return date;
    }

    public void setDate(LocalDateTime date){
        this.date.set(date);
    }

    public @NotNull String getDescription(){
        return description.get();
    }

    public SimpleStringProperty descriptionProperty(){
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    public @NotNull String getCode(){
        return code.get();
    }

    public SimpleStringProperty codeProperty(){
        return code;
    }

    public void setCode(String code){
        this.code.set(code);
    }

    @OneToOne
    public Storage getStore() {
        return store.get();
    }

    public SimpleObjectProperty<Storage> storeProperty() {
        return store;
    }

    public void setStore(Storage store) {
        this.store.set(store);
    }

    @OneToMany
    public Set<ProductMovementDetail> getPrice() {
        return price.get();
    }

    public ObjectProperty<Set<ProductMovementDetail>> priceProperty() {
        return price;
    }

    public void setPrice(Set<ProductMovementDetail> price) {
        this.price.set(price);
    }

}
