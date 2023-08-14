package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.Readonly;
import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.time.LocalDate;
import java.util.Set;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Product extends BeanImplementation<Product> {

    public Product() {
    }
    @TableViewColumn
    private SimpleStringProperty description = new SimpleStringProperty();
    @TableViewColumn(minWidth = 120, prefWidth = 150)
    private ObjectProperty<LocalDate> registrationDate = new SimpleObjectProperty<>();
    @TableViewColumn
    private SimpleStringProperty brand = new SimpleStringProperty();
    @TableViewColumn
    private SimpleStringProperty area = new SimpleStringProperty();
    @TableViewColumn
    private SimpleStringProperty barcode = new SimpleStringProperty();
    @TableViewColumn
    private SimpleStringProperty model = new SimpleStringProperty();
    @TableViewColumn
    private ObjectProperty<Unit> sellUnit = new SimpleObjectProperty<>();
    @TableViewColumn
    private ObjectProperty<Unit> purchaseUnit = new SimpleObjectProperty<>();
    @TableViewColumn
    @Readonly
    private SimpleIntegerProperty existence = new SimpleIntegerProperty();
    @TableViewColumn
    private SimpleBooleanProperty blocked = new SimpleBooleanProperty();
    @TableViewColumn
    private SimpleStringProperty imagePath = new SimpleStringProperty();
    @TableViewColumn
    private SimpleObjectProperty<Discount> discount = new SimpleObjectProperty<Discount>();
    @TableViewColumn
    private SimpleObjectProperty<Storage> storage = new SimpleObjectProperty<Storage>();
    @TableViewColumn
    private ObjectProperty<Set<Price>> price = new SimpleObjectProperty<>();

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
    @OneToOne
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

    @OneToMany
    public Set<Price> getPrice() {
        return price.get();
    }

    public ObjectProperty<Set<Price>> priceProperty() {
        return price;
    }

    public void setPrice(Set<Price> price) {
        this.price.set(price);
    }

}
