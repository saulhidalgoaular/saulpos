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
import com.saulpos.javafxcrudgenerator.annotations.Readonly;
import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import org.hibernate.annotations.Where;

import java.time.LocalDate;
import java.util.Set;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Product extends BeanImplementation<Product> {

    public Product() {
    }
    @TableViewColumn
    private final SimpleStringProperty description = new SimpleStringProperty();
    @TableViewColumn(minWidth = 120, prefWidth = 150)
    private final ObjectProperty<LocalDate> registrationDate = new SimpleObjectProperty<>();
    @TableViewColumn
    private final SimpleStringProperty brand = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleStringProperty area = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleStringProperty barcode = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleStringProperty model = new SimpleStringProperty();
    @TableViewColumn
    private final ObjectProperty<Unit> sellUnit = new SimpleObjectProperty<>();
    @TableViewColumn
    private final ObjectProperty<Unit> purchaseUnit = new SimpleObjectProperty<>();
    @TableViewColumn
    private final SimpleIntegerProperty existence = new SimpleIntegerProperty();
    @TableViewColumn
    private final SimpleBooleanProperty blocked = new SimpleBooleanProperty();
    @TableViewColumn
    private final SimpleStringProperty imagePath = new SimpleStringProperty();
    @TableViewColumn
    private final SimpleObjectProperty<Discount> discount = new SimpleObjectProperty<Discount>();
    @TableViewColumn
    private final SimpleObjectProperty<Storage> storage = new SimpleObjectProperty<Storage>();
    @TableViewColumn
    @Ignore
    private final ObjectProperty<Set<Price>> priceList = new SimpleObjectProperty<>();
    @TableViewColumn
    @Readonly
    private SimpleDoubleProperty price = new SimpleDoubleProperty(0);

    private final SimpleObjectProperty<Vat> vat = new SimpleObjectProperty<>();

    @NotNull
    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    @NotNull
    public LocalDate getRegistrationDate() {
        return registrationDate.get();
    }

    public ObjectProperty<LocalDate> registrationDateProperty() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate.set(registrationDate);
    }

    public String getBrand() {
        return brand.get();
    }

    public SimpleStringProperty brandProperty() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand.set(brand);
    }

    public String getArea() {
        return area.get();
    }

    public SimpleStringProperty areaProperty() {
        return area;
    }

    public void setArea(String area) {
        this.area.set(area);
    }

    @NotNull
    public String getBarcode() {
        return barcode.get();
    }

    public SimpleStringProperty barcodeProperty() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode.set(barcode);
    }

    @NotNull
    public String getModel() {
        return model.get();
    }

    public SimpleStringProperty modelProperty() {
        return model;
    }

    public void setModel(String model) {
        this.model.set(model);
    }
    @NotNull
    @OneToOne
    public Unit getSellUnit() {
        return sellUnit.get();
    }

    public ObjectProperty<Unit> sellUnitProperty() {
        return sellUnit;
    }

    public void setSellUnit(Unit sellUnit) {
        this.sellUnit.set(sellUnit);
    }

    @NotNull
    @OneToOne
    public Unit getPurchaseUnit() {
        return purchaseUnit.get();
    }

    public @NotNull ObjectProperty<Unit> purchaseUnitProperty() {
        return purchaseUnit;
    }

    public void setPurchaseUnit(Unit purchaseUnit) {
        this.purchaseUnit.set(purchaseUnit);
    }

    @NotNull
    public int getExistence() {
        return existence.get();
    }

    public SimpleIntegerProperty existenceProperty() {
        return existence;
    }

    public void setExistence(int existence) {
        this.existence.set(existence);
    }

    @NotNull
    public boolean isBlocked() {
        return blocked.get();
    }

    public SimpleBooleanProperty blockedProperty() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked.set(blocked);
    }

    public String getImagePath() {
        return imagePath.get();
    }

    public SimpleStringProperty imagePathProperty() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath.set(imagePath);
    }

    @NotNull
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "discount_id", referencedColumnName = "id")
    @Where(clause = "beanStatus = 'Active' ")
    public Discount getDiscount() {
        return discount.get();
    }

    public SimpleObjectProperty<Discount> discountProperty() {
        return discount;
    }

    public void setDiscount(Discount discount) {
        this.discount.set(discount);
    }

    @NotNull
    @OneToOne
    public Storage getStorage() {
        return storage.get();
    }

    public SimpleObjectProperty<Storage> storageProperty() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage.set(storage);
    }

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Where(clause = "beanStatus = 'Active' ")
    public Set<Price> getPriceList() {
        return priceList.get();
    }

    public ObjectProperty<Set<Price>> priceListProperty() {
        return priceList;
    }

    public void setPriceList(Set<Price> priceList) {
        this.priceList.set(priceList);
    }

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "vat_id", referencedColumnName = "id")
    @Where(clause = "beanStatus = 'Active' ")
    public Vat getVat() {
        return vat.get();
    }

    public SimpleObjectProperty<Vat> vatProperty() {
        return vat;
    }

    public void setVat(Vat vat) {
        this.vat.set(vat);
    }

    @Transient
    public SimpleDoubleProperty getCurrentPrice(){
        //Calculate price for current day.
        Set<Price> priceSet = getPriceList();
        if(priceSet != null){
            for(Price price: priceSet){
                LocalDate now = LocalDate.now();
                //Check the current time is equal or within the boundary time.
                if( (now.isEqual(price.getFromDate()) || now.isAfter(price.getFromDate()))
                        && (now.isEqual(price.getToDate()) || now.isBefore(price.getToDate())) ){
                    return price.priceProperty();
                }
            }
        }
        return new SimpleDoubleProperty(0f);
    }

    @Transient
    public double getPrice() {
        return price.get();
    }

    public SimpleDoubleProperty priceProperty() {
        this.price.set(getCurrentPrice().getValue());
        return price;
    }

    public void setPrice(double price) {
        this.price.set(price);
    }

    @Transient
    public StringBinding getCurrentDiscountString(){
        return (StringBinding) Bindings.concat(getCurrentDiscount().asString(), new SimpleStringProperty("%"));
    }

    @Transient
    public SimpleDoubleProperty getCurrentDiscount(){
        //check if discount is available till now?
        Discount discount = getDiscount();
        LocalDate now = LocalDate.now();
        if(discount != null
                && (now.isEqual(discount.getStartingDate()) || now.isAfter(discount.getStartingDate()) )
                && (now.isEqual(discount.getEndingDate()) || now.isBefore(discount.getEndingDate()) )
        ){
            return discount.percentageProperty();
        }
        return new SimpleDoubleProperty(0);
    }
    @Transient
    public DoubleBinding getVatAmount(){
        if (getVat() == null){
            return Bindings.createDoubleBinding(() -> .0);
        }
        SimpleDoubleProperty price = getCurrentPrice();

        return price.multiply(getVat().percentageProperty()).divide(100);
    }
    @Transient
    public DoubleBinding getTotalAmount(){
        DoubleBinding discountAmount = getCurrentPrice().multiply(getCurrentDiscount()).divide(100).negate();
        return getVatAmount().add(getCurrentPrice()).add(discountAmount);
    }

    @Override
    public String toString() {
        return description.get();
    }
}
