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
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
//TODO check the keys

@Entity
@Access(AccessType.PROPERTY)
@Table
public class ProductMovementDetail extends BeanImplementation<ProductMovementDetail> {

    private final SimpleObjectProperty<Product> product = new SimpleObjectProperty<>();
    private final SimpleIntegerProperty amount = new SimpleIntegerProperty();
    private final SimpleStringProperty type = new SimpleStringProperty();
    private final SimpleObjectProperty<ProductMovement> productMovement = new SimpleObjectProperty<ProductMovement>();

    @OneToOne
    @NotNull
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
    public int getAmount() {
        return amount.get();
    }

    public void setAmount(int amount) {
        this.amount.set(amount);
    }

    public SimpleIntegerProperty amountProperty() {
        return amount;
    }

    public String getType() {
        return type.get();
    }

    public void setType(String type) {
        this.type.set(type);
    }

    public SimpleStringProperty typeProperty() {
        return type;
    }

    @ManyToOne
    @NotNull
    public ProductMovement getProductMovement() {
        return productMovement.get();
    }

    public SimpleObjectProperty<ProductMovement> productMovementProperty() {
        return productMovement;
    }

    public void setProductMovement(ProductMovement productMovement) {
        this.productMovement.set(productMovement);
    }

    public ProductMovementDetail() {

    }
}
