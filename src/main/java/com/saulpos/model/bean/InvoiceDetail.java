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
public class InvoiceDetail extends BeanImplementation<InvoiceDetail> {

    //codigo_de_articulo : internal invoice code
    private final ObjectProperty<Invoice> invoice = new SimpleObjectProperty<Invoice>();

    //codigo_de_articulo
    @OneToOne
    private final SimpleObjectProperty<Product> product = new SimpleObjectProperty<Product>();

    //cantidad
    @ColumnDefault("1")
    private final SimpleIntegerProperty amount = new SimpleIntegerProperty();

    //devuelto
    private final SimpleIntegerProperty cancelled = new SimpleIntegerProperty();

    //precio_venta
    @ColumnDefault("0.00")
    private final SimpleDoubleProperty salePrice = new SimpleDoubleProperty();

    //descuento
    @ColumnDefault("0.00")
    private final SimpleDoubleProperty discount = new SimpleDoubleProperty();

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

    public @NotNull int getAmount() {
        return amount.get();
    }

    public void setAmount(int amount) {
        this.amount.set(amount);
    }

    public SimpleIntegerProperty amountProperty() {
        return amount;
    }

    public @NotNull int getCancelled() {
        return cancelled.get();
    }

    public void setCancelled(int cancelled) {
        this.cancelled.set(cancelled);
    }

    public SimpleIntegerProperty cancelledProperty() {
        return cancelled;
    }

    public @NotNull Double getSalePrice() {
        return salePrice.get();
    }

    public void setSalePrice(Double salePrice) {
        this.salePrice.set(salePrice);
    }

    public SimpleDoubleProperty salePriceProperty() {
        return salePrice;
    }

    public @NotNull Double getDiscount() {
        return discount.get();
    }

    public void setDiscount(Double discount) {
        this.discount.set(discount);
    }

    public SimpleDoubleProperty discountProperty() {
        return discount;
    }

    @ManyToOne(cascade = CascadeType.MERGE)
    public Invoice getInvoice() {
        return invoice.get();
    }

    public ObjectProperty<Invoice> invoiceProperty() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice.set(invoice);
    }

//    public void setSalePrice(double salePrice) {
//        this.salePrice.set(salePrice);
//    }

//    public void setDiscount(double discount) {
//        this.discount.set(discount);
//    }

}
