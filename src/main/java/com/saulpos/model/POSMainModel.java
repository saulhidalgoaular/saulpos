package com.saulpos.model;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementationSoftDelete;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.bean.DollarRate;
import com.saulpos.model.bean.Invoice;
import com.saulpos.model.bean.Product;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.DatabaseConnection;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.util.Duration;

import java.beans.PropertyVetoException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class POSMainModel extends AbstractModel{

    private UserB userB;
    private SimpleObjectProperty<Invoice> invoiceInProgress = new SimpleObjectProperty<>();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private SimpleStringProperty clockValue = new SimpleStringProperty();
    private SimpleStringProperty dateValue = new SimpleStringProperty();

    private SimpleStringProperty employeeName = new SimpleStringProperty();

    private ObservableList<Invoice> invoiceWaiting = FXCollections.observableArrayList();

    private SimpleStringProperty barcodeBar = new SimpleStringProperty();

    private SimpleDoubleProperty total = new SimpleDoubleProperty(0);

    private SimpleDoubleProperty totalVat = new SimpleDoubleProperty(0);

    private SimpleDoubleProperty totalUSD = new SimpleDoubleProperty(0);

    private SimpleDoubleProperty subtotal = new SimpleDoubleProperty(0);
    private SimpleObjectProperty<DollarRate> activeDollarRate = new SimpleObjectProperty<>();

    public POSMainModel(UserB userB) throws PropertyVetoException {
        this.userB = userB;
        invoiceInProgress.set(new Invoice());
        initialize();
    }

    @Override
    public void addChangedListeners() {
        // Think where to add the bindings. If we add them in the bean, it
        // might bring inconsistencies while we load it from database.

    }


    @Override
    public void addListeners() {
        invoiceInProgress.getValue().getProducts().addListener(new ListChangeListener<Product>() {
            @Override
            public void onChanged(Change<? extends Product> change) {
                total.set(invoiceInProgress.getValue().getProducts().stream().mapToDouble(
                        value -> value.getTotalAmount().getValue()
                ).sum());

                totalUSD.set( invoiceInProgress.getValue().getProducts().stream()
                        .collect(Collectors.summingDouble(
                                value -> convertToDollar(value.getTotalAmount().getValue()).getValue()
//                                    value.getCurrentPrice().getValue() + value.getVatAmount().getValue();

                        )));

                subtotal.set(invoiceInProgress.getValue().getProducts().stream().mapToDouble(value -> {
                    Double discountAmount = value.getCurrentPrice().multiply(value.getCurrentDiscount()).divide(100).getValue();
                    return value.getCurrentPrice().getValue() - discountAmount;
                }).sum());

                totalVat.set(invoiceInProgress.getValue().getProducts().stream()
                        .collect(Collectors.summingDouble(
                                value -> value.getVatAmount().getValue()

                        )));
            }
        });
    }

    @Override
    public void addDataSource() throws PropertyVetoException {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(1), event -> {
                    LocalDateTime now = LocalDateTime.now();

                    clockValue.set(now.format(TIME_FORMATTER));
                    dateValue.set(now.format(DATE_FORMATTER));
                })
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        employeeName.set(userB.getName());
    }

    public UserB getUserB() {
        return userB;
    }

    public void setUserB(UserB userB) {
        this.userB = userB;
    }

    public String getDateValue() {
        return dateValue.get();
    }

    public void setDateValue(String dateValue) {
        this.dateValue.set(dateValue);
    }

    public SimpleStringProperty dateValueProperty() {
        return dateValue;
    }

    public String getClockValue() {
        return clockValue.get();
    }

    public void setClockValue(String clockValue) {
        this.clockValue.set(clockValue);
    }

    public SimpleStringProperty clockValueProperty() {
        return clockValue;
    }

    public String getEmployeeName() {
        return employeeName.get();
    }

    public void setEmployeeName(String employeeName) {
        this.employeeName.set(employeeName);
    }

    public SimpleStringProperty employeeNameProperty() {
        return employeeName;
    }

    public String getBarcodeBar() {
        return barcodeBar.get();
    }

    public void setBarcodeBar(String barcodeBar) {
        this.barcodeBar.set(barcodeBar);
    }

    public SimpleStringProperty barcodeBarProperty() {
        return barcodeBar;
    }

    public ObservableList<Invoice> getInvoiceWaiting() {
        return invoiceWaiting;
    }

    public void setInvoiceWaiting(ObservableList<Invoice> invoiceWaiting) {
        this.invoiceWaiting = invoiceWaiting;
    }

    public Invoice getInvoiceInProgress() {
        return invoiceInProgress.get();
    }

    public void setInvoiceInProgress(Invoice invoiceInProgress) {
        this.invoiceInProgress.set(invoiceInProgress);
    }

    public double getTotal() {
        return total.get();
    }

    public SimpleDoubleProperty totalProperty() {
        return total;
    }

    public void setTotal(double total) {
        this.total.set(total);
    }

    public double getTotalVat() {
        return totalVat.get();
    }

    public SimpleDoubleProperty totalVatProperty() {
        return totalVat;
    }

    public void setTotalVat(double totalVat) {
        this.totalVat.set(totalVat);
    }

    public double getTotalUSD() {
        return totalUSD.get();
    }

    public SimpleDoubleProperty totalUSDProperty() {
        return totalUSD;
    }

    public void setTotalUSD(double totalUSD) {
        this.totalUSD.set(totalUSD);
    }

    public double getSubtotal() {
        return subtotal.get();
    }

    public SimpleDoubleProperty subtotalProperty() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal.set(subtotal);
    }

    public SimpleObjectProperty<Invoice> invoiceInProgressProperty() {
        return invoiceInProgress;
    }

    public DollarRate getActiveDollarRate() {
        return activeDollarRate.get();
    }

    public SimpleObjectProperty<DollarRate> activeDollarRateProperty() {
        return activeDollarRate;
    }

    public void setActiveDollarRate(DollarRate activeDollarRate) {
        this.activeDollarRate.set(activeDollarRate);
    }

    public void addItem() throws Exception {
        Product product = new Product();
        product.setBarcode(barcodeBar.getValue());
        final List<Product> list = DatabaseConnection.getInstance().listBySample(Product.class, product, AbstractDataProvider.SearchType.EQUAL);
        if (list.size() == 1 && list.get(0).getExistence() > 0) {
            invoiceInProgress.get().getProducts().add(list.get(0));
            list.get(0).setExistence(list.get(0).getExistence() -1);
            list.get(0).saveOrUpdate();
            barcodeBar.setValue("");
        }
    }

    public void removeItem(TableView<Product> itemsTableView) throws Exception {
        int selectedIndex = itemsTableView.getSelectionModel().getSelectedIndex();
        Product removedProduct = itemsTableView.getItems().get(selectedIndex);
        removedProduct.setExistence(removedProduct.getExistence() + 1);
        itemsTableView.getItems().remove(selectedIndex);
        removedProduct.saveOrUpdate();

    }

    public DoubleBinding convertToDollar(double localCurrency){
        return getActiveDollarRate() != null ?
            getActiveDollarRate().localCurrencyRateProperty().multiply(localCurrency) :
                Bindings.createDoubleBinding(() -> localCurrency);
    }

    public DollarRate findActiveDollarRate() {
        try {
            String query = "SELECT * FROM dollarrate WHERE activated=1";
            List<Object[]> allItems = DatabaseConnection.getInstance().runQuery(query);
            if(allItems.size() == 1){
                DollarRate entity= new DollarRate();
//                DialogBuilder.createInformation("Info", "SAUL POS", "Active size: "+allItems.size()).showAndWait();
                Object[] objArr = allItems.getFirst();
                entity.setId(Integer.parseInt(objArr[0].toString()));
                entity.setBeanStatus(AbstractBeanImplementationSoftDelete.BeanStatus.valueOf(objArr[1].toString()));
                entity.setCreationTime(LocalDateTime.parse(objArr[2].toString().replace(" ", "T")));
                if(objArr[3] != null){
                    entity.setLastModificationTime(LocalDateTime.parse(objArr[3].toString().replace(" ", "T")));
                }
                if(objArr[4] != null){
                    entity.setLocalCurrencyName(objArr[4].toString());
                }
                if(objArr[5] != null){
                    entity.setLocalCurrencyRate(Double.parseDouble(objArr[5].toString()));
                }
                entity.setActivated(true);
                return entity;
            }else if(allItems.size() == 0){
                DialogBuilder.createWarning("Warning", "SAUL POS",
                        "No dollar rate is activated. Please activate one from admin module.").showAndWait();
            }else{
                DialogBuilder.createWarning("Warning", "SAUL POS",
                        "Multiple dollar rate is activated. Please activate only one from admin module.").showAndWait();
            }
        } catch (Exception e) {
            DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
        }
        return null;
    }
}
