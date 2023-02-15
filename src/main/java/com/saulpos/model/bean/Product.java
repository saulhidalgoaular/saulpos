package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;

import java.time.LocalDate;


@Entity
@Access(AccessType.PROPERTY)
@Table
public class Product extends AbstractBeanImplementation {

    public Product() {
    }

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    private SimpleStringProperty description = new SimpleStringProperty();

    private ObjectProperty<LocalDate> registrationDate = new SimpleObjectProperty<>();

    private SimpleStringProperty brand = new SimpleStringProperty();

    private SimpleStringProperty area = new SimpleStringProperty();

    private SimpleStringProperty barcode = new SimpleStringProperty();

    private SimpleStringProperty model = new SimpleStringProperty();

    private ObjectProperty<Unit> sellUnit = new SimpleObjectProperty<>();

    private ObjectProperty<Unit> purchaseUnit = new SimpleObjectProperty<>();

    private SimpleIntegerProperty existence = new SimpleIntegerProperty();

    private SimpleBooleanProperty blocked = new SimpleBooleanProperty();

    private SimpleStringProperty imagePath = new SimpleStringProperty();

    private SimpleObjectProperty<Discount> discount = new SimpleObjectProperty<>();

    private SimpleObjectProperty<Storage> storage = new SimpleObjectProperty<>();

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public String getDescription() {
        return description.get();
    }

    public @NotNull SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    @NotNull
    public LocalDate getRegistrationDate() {
        return registrationDate.get();
    }

    public @NotNull ObjectProperty<LocalDate> registrationDateProperty() {
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
    @Column(nullable = false)
    public String getBarcode() {
        return barcode.get();
    }

    public @NotNull SimpleStringProperty barcodeProperty() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode.set(barcode);
    }

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

    public @NotNull SimpleIntegerProperty existenceProperty() {
        return existence;
    }

    public void setExistence(int existence) {
        this.existence.set(existence);
    }

    @NotNull
    public boolean isBlocked() {
        return blocked.get();
    }

    public @NotNull SimpleBooleanProperty blockedProperty() {
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

    public @NotNull SimpleObjectProperty<Discount> discountProperty() {
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

    @Override
    public void receiveChanges(AbstractBeanImplementation currentBean) {
        // TODO
    }

    @Override
    public AbstractBeanImplementation clone() {
        // TODO
        return null;
    }
}