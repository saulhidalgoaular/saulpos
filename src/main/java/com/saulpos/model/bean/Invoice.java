package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.LocalDateTime;

//Todo check the keys again

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Invoice extends AbstractBeanImplementation {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    //codigo_interno
    private SimpleStringProperty internalCode = new SimpleStringProperty();

    //estado enum('Pedido','Anulada','Espera','Facturada') NOT NULL,
    //Todo the enum will be stored as string or not ?
    //I search for this I saw string is better than enum
    //https://stackoverflow.com/questions/229856/ways-to-save-enums-in-database


    //fecha_creacion
    private ObjectProperty<LocalDateTime> creationDate = new SimpleObjectProperty<>();

    //fecha_impresion
    private ObjectProperty<LocalDateTime> printingDate = new SimpleObjectProperty<>();

    //codigo_de_cliente
    private SimpleStringProperty clientCode = new SimpleStringProperty();

    //total_sin_iva
    private SimpleDoubleProperty totalWithoutVat = new SimpleDoubleProperty();

    //total_con_iva
    private SimpleDoubleProperty totalWithVat = new SimpleDoubleProperty();

    //descuento_global
    @ColumnDefault("0.0000")
    private SimpleDoubleProperty globalDiscount = new SimpleDoubleProperty();

    //iva
    private SimpleDoubleProperty vat = new SimpleDoubleProperty();

    //impresora
    private SimpleStringProperty printer = new SimpleStringProperty();

    //numero_fiscal
    private SimpleStringProperty taxNumber = new SimpleStringProperty();

    //numero_reporte_z
    private SimpleStringProperty reportzNumber = new SimpleStringProperty();

    //codigo_de_usuario
    private SimpleStringProperty userCode = new SimpleStringProperty();

    //cantidad_de_articulos
    private SimpleIntegerProperty articlesQuantity = new SimpleIntegerProperty();

    //identificador_turno
    private SimpleStringProperty shiftIdentifier = new SimpleStringProperty();

    //identificador_pos
    private SimpleStringProperty posIdentifier = new SimpleStringProperty();

    //codigo_interno_alternativo
    private SimpleStringProperty alternativeInternalCode = new SimpleStringProperty();

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

    public @NotNull String getInternalCode() {
        return internalCode.get();
    }

    public void setInternalCode(String internalCode) {
        this.internalCode.set(internalCode);
    }

    public SimpleStringProperty internalCodeProperty() {
        return internalCode;
    }

    public @NotNull LocalDateTime getCreationDate() {
        return creationDate.get();
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate.set(creationDate);
    }

    public ObjectProperty<LocalDateTime> creationDateProperty() {
        return creationDate;
    }

    public LocalDateTime getPrintingDate() {
        return printingDate.get();
    }

    public void setPrintingDate(LocalDateTime printingDate) {
        this.printingDate.set(printingDate);
    }

    public ObjectProperty<LocalDateTime> printingDateProperty() {
        return printingDate;
    }

    public String getClientCode() {
        return clientCode.get();
    }

    public void setClientCode(String clientCode) {
        this.clientCode.set(clientCode);
    }

    public SimpleStringProperty clientCodeProperty() {
        return clientCode;
    }

    public @NotNull Double getTotalWithOutVat() {
        return totalWithoutVat.get();
    }

    public void setTotalWithOutVat(Double totalWithoutVat) {
        this.totalWithoutVat.set(totalWithoutVat);
    }

    public SimpleDoubleProperty totalWithOutVatProperty() {
        return totalWithoutVat;
    }

    public @NotNull Double getTotalWithVat() {
        return totalWithVat.get();
    }

    public void setTotalWithVat(Double totalWithVat) {
        this.totalWithVat.set(totalWithVat);
    }

    public SimpleDoubleProperty totalWithVatProperty() {
        return totalWithVat;
    }

    public @NotNull Double getGlobalDiscount() {
        return globalDiscount.get();
    }

    public void setGlobalDiscount(Double globalDiscount) {
        this.globalDiscount.set(globalDiscount);
    }

    public SimpleDoubleProperty globalDiscountProperty() {
        return globalDiscount;
    }

    public @NotNull Double getVat() {
        return vat.get();
    }

    public void setVat(Double vat) {
        this.vat.set(vat);
    }

    public SimpleDoubleProperty vatProperty() {
        return vat;
    }

    public String getPrinter() {
        return printer.get();
    }

    public SimpleStringProperty printerProperty() {
        return printer;
    }

    public void setPrinter(String printer) {
        this.printer.set(printer);
    }

    public String getTaxNumber() {
        return taxNumber.get();
    }

    public SimpleStringProperty taxNumberProperty() {
        return taxNumber;
    }

    public void setTaxNumber(String taxNumber) {
        this.taxNumber.set(taxNumber);
    }

    public String getReportzNumber() {
        return reportzNumber.get();
    }

    public SimpleStringProperty reportzNumberProperty() {
        return reportzNumber;
    }

    public void setReportzNumber(String reportzNumber) {
        this.reportzNumber.set(reportzNumber);
    }

    public @NotNull String getUserCode() {
        return userCode.get();
    }

    public SimpleStringProperty userCodeProperty() {
        return userCode;
    }

    public void setUserCode(String userCode) {
        this.userCode.set(userCode);
    }

    public @NotNull int getArticlesQuantity() {
        return articlesQuantity.get();
    }

    public SimpleIntegerProperty articlesQuantityProperty() {
        return articlesQuantity;
    }

    public void setArticlesQuantity(int articlesQuantity) {
        this.articlesQuantity.set(articlesQuantity);
    }

    public @NotNull String getShiftIdentifier() {
        return shiftIdentifier.get();
    }

    public SimpleStringProperty shiftIdentifierProperty() {
        return shiftIdentifier;
    }

    public void setShiftIdentifier(String shiftIdentifier) {
        this.shiftIdentifier.set(shiftIdentifier);
    }

    public @NotNull String getPosIdentifier() {
        return posIdentifier.get();
    }

    public SimpleStringProperty posIdentifierProperty() {
        return posIdentifier;
    }

    public void setPosIdentifier(String posIdentifier) {
        this.posIdentifier.set(posIdentifier);
    }

    public @NotNull String getAlternativeInternalCode() {
        return alternativeInternalCode.get();
    }

    public SimpleStringProperty alternativeInternalCodeProperty() {
        return alternativeInternalCode;
    }

    public void setAlternativeInternalCode(String alternativeInternalCode) {
        this.alternativeInternalCode.set(alternativeInternalCode);
    }

    @Override
    public void receiveChanges(AbstractBeanImplementation currentBean) {
        //Todo
    }

    @Override
    public AbstractBeanImplementation clone() {
        //Todo
        return null;
    }
}
