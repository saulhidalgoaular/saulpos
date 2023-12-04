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
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class CreditNoteDetails extends BeanImplementation<CreditNoteDetails> {

    private final ObjectProperty<CreditNote> creditNote = new SimpleObjectProperty<>();
    private final SimpleObjectProperty<Product> product = new SimpleObjectProperty<Product>();
    private final SimpleIntegerProperty amount = new SimpleIntegerProperty();
    private final SimpleIntegerProperty cancelled = new SimpleIntegerProperty();
    private final SimpleDoubleProperty salePrice = new SimpleDoubleProperty();
    private final SimpleDoubleProperty discount = new SimpleDoubleProperty();

    @ManyToOne
    @NotNull
    public CreditNote getCreditNote() {
        return creditNote.get();
    }

    public ObjectProperty<CreditNote> creditNoteProperty() {
        return creditNote;
    }

    public void setCreditNote(CreditNote creditNote) {
        this.creditNote.set(creditNote);
    }

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
    @ColumnDefault("1")
    @NotNull
    public int getAmount() {
        return amount.get();
    }

    public SimpleIntegerProperty amountProperty() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount.set(amount);
    }
    @ColumnDefault("0")
    @NotNull
    public int getCancelled() {
        return cancelled.get();
    }

    public SimpleIntegerProperty cancelledProperty() {
        return cancelled;
    }

    public void setCancelled(int cancelled) {
        this.cancelled.set(cancelled);
    }

    @ColumnDefault("0.00")
    @NotNull
    public double getSalePrice() {
        return salePrice.get();
    }

    public SimpleDoubleProperty salePriceProperty() {
        return salePrice;
    }

    public void setSalePrice(double salePrice) {
        this.salePrice.set(salePrice);
    }
    @ColumnDefault("0.00")
    @NotNull
    public double getDiscount() {
        return discount.get();
    }

    public SimpleDoubleProperty discountProperty() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount.set(discount);
    }

}
