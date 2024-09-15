package com.saulpos.model;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.bean.*;
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
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.beans.PropertyVetoException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        Invoice invoice = new Invoice();
        invoice.setInvoiceDetails(new HashSet<InvoiceDetail>());
        invoiceInProgress.set(invoice);
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
                calculateProductsCostDetails();
            }
        });
        invoiceWaiting.addListener(new ListChangeListener<Invoice>() {
            @Override
            public void onChanged(Change<? extends Invoice> change) {
                calculateProductsCostDetails();
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
            Product productToAdd = list.get(0);
            invoiceInProgress.get().getProducts().add(productToAdd);
            addProductToInvoiceDetails(productToAdd);
            productToAdd.setExistence(productToAdd.getExistence() -1);
            productToAdd.saveOrUpdate();
            barcodeBar.setValue("");
            if(invoiceInProgress.get().getCreationDate() == null){
                invoiceInProgress.get().setCreationDate(LocalDateTime.now());
            }
            System.out.println("Invoice Details size: " + invoiceInProgress.get().getInvoiceDetails().size());
        }
    }

    private void addProductToInvoiceDetails(Product productToAdd) {
        InvoiceDetail invoiceDetail = new InvoiceDetail();
        invoiceDetail.setInvoice(invoiceInProgress.get());
        invoiceDetail.setProduct(productToAdd);
        invoiceDetail.setSalePrice(productToAdd.priceProperty().get());
        invoiceDetail.setAmount(1);
        invoiceDetail.setDiscount(productToAdd.getCurrentDiscount().get());
        invoiceDetail.setCancelled(0);
        invoiceDetail.setCreationTime(LocalDateTime.now());
        invoiceInProgress.get().getInvoiceDetails().add(invoiceDetail);
    }

    public void removeItem(TableView<Product> itemsTableView) throws Exception {
        int selectedIndex = itemsTableView.getSelectionModel().getSelectedIndex();
        Product removedProduct = itemsTableView.getItems().get(selectedIndex);
        removedProduct.setExistence(removedProduct.getExistence() + 1);
        itemsTableView.getItems().remove(selectedIndex);
        removedProduct.saveOrUpdate();
        removeProductFromInvoiceDetails(removedProduct);
        System.out.println("Invoice Details size: " + invoiceInProgress.get().getInvoiceDetails().size());
    }

    private void removeProductFromInvoiceDetails(Product removedProduct) {
        Set<InvoiceDetail> invoiceDetails = invoiceInProgress.get().getInvoiceDetails();
        for(InvoiceDetail invoiceDetail: invoiceDetails){
            if(invoiceDetail.getProduct().getId() == removedProduct.getId()){
                invoiceDetails.remove(invoiceDetail);
                break;
            }
        }
    }

    public DoubleBinding convertToDollar(double localCurrency){
        return getActiveDollarRate() != null ?
            getActiveDollarRate().localCurrencyRateProperty().multiply(localCurrency) :
                Bindings.createDoubleBinding(() -> localCurrency);
    }

    public DollarRate findActiveDollarRate() {
        try {
            DollarRate dollar = new DollarRate();
            dollar.setLocalCurrencyName("Bolivar");
            dollar.setId(1);
            dollar.setActivated(true);
            return dollar;
        } catch (Exception e) {
            DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
        }
        return null;
    }

    public void invoiceInProgressToWaiting(TableView<Product> itemsTableView, GridPane clientInfoGrid){
        // Return if there is no product in table view.
        if(itemsTableView.getItems().size() == 0){
            DialogBuilder.createError("Error!", "SAUL POS",
                    "There is no product in product list").showAndWait();
            return;
        }
        System.out.println("Moving Current invoice in waiting state!");
        //Considering only one invoice can be in waiting state.
        if(getInvoiceWaiting().size() > 0){
            DialogBuilder.createError("Error!", "SAUL POS",
                    "Already an invoice in waiting state. Another invoice is not allowed to move in waiting state.").showAndWait();
        } else if (getInvoiceWaiting().size() == 0) {
            //Move inProgress invoice data into the waiting invoice data in model.
            Invoice waitingInvoice = new Invoice();
            waitingInvoice.setStatus(Invoice.InvoiceStatus.Waiting);

            //Move all products from inProgress invoice to waiting invoice & clear products from inProgress invoice
            ObservableList<Product> products = FXCollections.observableArrayList();
            products.addAll(getInvoiceInProgress().getProducts());
            waitingInvoice.setProducts(products);
            getInvoiceInProgress().getProducts().clear();

            //Move invoice details from in progress invoice to waiting invoice & clear from inProgress invoice.
            Set<InvoiceDetail> invoiceDetails = new HashSet<>(getInvoiceInProgress().getInvoiceDetails());
            waitingInvoice.setInvoiceDetails(invoiceDetails);
            getInvoiceInProgress().getInvoiceDetails().clear();

            //Move client from inProgress invoice to waiting invoice & set client=null in inProgress invoice.
            //And hide the clientInfoGrid
            Client inProgressClient = getInvoiceInProgress().getClient();
            if(inProgressClient != null){
                Client waitingClient = new Client();
                waitingClient.setId(inProgressClient.getId());
                if(inProgressClient.getName() != null){
                    waitingClient.setName(inProgressClient.getName());
                }
                if (inProgressClient.getAddress() != null) {
                    waitingClient.setAddress(inProgressClient.getAddress());
                }
                if (inProgressClient.getPhone() != null) {
                    waitingClient.setPhone(inProgressClient.getPhone());
                }
                waitingInvoice.setClient(waitingClient);
                getInvoiceInProgress().setClient(null);
                clientInfoGrid.setVisible(false);
            }
            // Adding this waitingInvoice into the model & show info dialog.
            getInvoiceWaiting().add(waitingInvoice);
            DialogBuilder.createInformation("Success!", "SAUL POS",
                    "Current invoice moved into waiting state & Global discount(if applied) is canceled!").showAndWait();
        }
    }

    public void invoiceWaitingToInProgress(GridPane clientInfoGrid){
        System.out.println("Restore waiting invoice from waiting list.");
        // Return if there is no invoice in waiting state
        if(getInvoiceWaiting().size() == 0){
            DialogBuilder.createInformation("Info!", "SAUL POS", "No invoice in waiting state.").showAndWait();
        }else if(getInvoiceWaiting().size() == 1){
            //Clear the product list from inProgress invoice and restore products from waiting invoice.
            Invoice waitingInvoice = getInvoiceWaiting().getFirst();
            getInvoiceInProgress().getProducts().clear();
            getInvoiceInProgress().getProducts().addAll(waitingInvoice.getProducts());

            //Clear the invoice details from inProgress invoice and restore invoice details from waiting invoice.
            getInvoiceInProgress().getInvoiceDetails().clear();
            getInvoiceInProgress().getInvoiceDetails().addAll(waitingInvoice.getInvoiceDetails());

            //Clear the client info from inProgress invoice and restore client info from waiting invoice.
            if(waitingInvoice.getClient() == null){
                getInvoiceInProgress().setClient(null);
                clientInfoGrid.setVisible(false);
            }else{
                getInvoiceInProgress().setClient(waitingInvoice.getClient());
                clientInfoGrid.setVisible(true);
                ((Label) clientInfoGrid.getChildren().get(1)).setText(getInvoiceInProgress().getClient().getName());
                ((Label) clientInfoGrid.getChildren().get(3)).setText(getInvoiceInProgress().getClient().getAddress());
                ((Label) clientInfoGrid.getChildren().get(5)).setText(getInvoiceInProgress().getClient().getPhone());
            }
            //Clear the waiting invoice list & show info dialog.
            getInvoiceWaiting().clear();
            DialogBuilder.createInformation("Success!", "SAUL POS",
                    "Current invoice is restored from waiting state!").showAndWait();
        }
    }

    public void transferMoney(){
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("SAUL POS");
        alert.setHeaderText("Login and Transfer");
        // Create a grid pane for the form
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter Username");
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        Label amountLabel = new Label("Amount:");
        TextField amountField = new TextField();
        amountField.setPromptText("Amount to Extract");
        //Only accept numbers
        amountField.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if(!Character.isDigit(e.getCharacter().charAt(0))){
                e.consume();
            }
        });
        // Add form fields to the grid pane
        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);
        gridPane.add(amountLabel, 0, 2);
        gridPane.add(amountField, 1, 2);
        // Set the grid pane as the alert dialog's content
        alert.getDialogPane().setContent(gridPane);
        // Add buttons to the alert dialog
        ButtonType okButtonType = ButtonType.OK;
        alert.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        //Hide Ok button from Alert if any field is empty.
        TextField[] textFields = {usernameField, passwordField, amountField};
        for(TextField item: textFields){
            item.textProperty().addListener((observable, oldValue, newValue) -> {
                if(!usernameField.getText().trim().isEmpty() && !passwordField.getText().trim().isEmpty() && !amountField.getText().trim().isEmpty()){
                    if(!alert.getDialogPane().getButtonTypes().contains(okButtonType)){
                        alert.getDialogPane().getButtonTypes().add(0, okButtonType);
                        item.requestFocus();
                    }
                }else{
                    alert.getDialogPane().getButtonTypes().remove(okButtonType);
                }
            });
        }
        // Show the alert dialog
        alert.showAndWait().ifPresent(response -> {
            if (response == okButtonType ) {
                // Handle form submission here
                //Check the credentials and transfer money.
                System.out.println("Username: " + usernameField.getText());
                System.out.println("Password: " + passwordField.getText());
                System.out.println("Amount: " + amountField.getText());
            }
        });
    }

    public void applyGlobalDiscount() {
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("SAUL POS");
        alert.setHeaderText("Global Discount");
        // Create a grid pane for the form
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(20));
        Label usernameLabel = new Label("Username:");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter Username");
        Label passwordLabel = new Label("Password:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");
        Label amountLabel = new Label("Global Discount:");
        TextField discountField = new TextField();
        discountField.setPromptText("(%)");
        //Only accept numbers
        discountField.addEventFilter(KeyEvent.KEY_TYPED, e -> {
            if(!Character.isDigit(e.getCharacter().charAt(0))){
                e.consume();
            }
        });
        // Add form fields to the grid pane
        gridPane.add(usernameLabel, 0, 0);
        gridPane.add(usernameField, 1, 0);
        gridPane.add(passwordLabel, 0, 1);
        gridPane.add(passwordField, 1, 1);
        gridPane.add(amountLabel, 0, 2);
        gridPane.add(discountField, 1, 2);
        // Set the grid pane as the alert dialog's content
        alert.getDialogPane().setContent(gridPane);
        // Add buttons to the alert dialog
        ButtonType okButtonType = ButtonType.OK;
        alert.getDialogPane().getButtonTypes().addAll(ButtonType.CANCEL);

        //Hide Ok button from Alert if any field is empty.
        TextField[] textFields = {usernameField, passwordField, discountField};
        for(TextField item: textFields){
            item.textProperty().addListener((observable, oldValue, newValue) -> {
                if(!usernameField.getText().trim().isEmpty() && !passwordField.getText().trim().isEmpty() && !discountField.getText().trim().isEmpty()){
                    if(!alert.getDialogPane().getButtonTypes().contains(okButtonType)){
                        alert.getDialogPane().getButtonTypes().add(0, okButtonType);
                        item.requestFocus();
                    }
                }else{
                    alert.getDialogPane().getButtonTypes().remove(okButtonType);
                }
            });
        }
        // Show the alert dialog
        alert.showAndWait().ifPresent(response -> {
            if (response == okButtonType ) {
                //Check the credentials and apply discount.
                if(validateGlobalDiscount(usernameField.getText(), passwordField.getText(), discountField.getText())){
                    double discount = Double.parseDouble(discountField.getText());
                    setTotalUSD(totalUSD.subtract(totalUSD.multiply(discount).divide(100)).getValue());
                    getInvoiceInProgress().setGlobalDiscount(discount);
                    DialogBuilder.createInformation("Info!", "SAUL POS", "Discount implemented successfully.").showAndWait();
                }else{
                    DialogBuilder.createError("Error!", "SAUL POS", "Invalid Credentials or discount!").showAndWait();
                }
            }
        });
    }

    private boolean validateGlobalDiscount(String username, String password, String discount) {
        //Fixme -- Implement this method based on requirements
        // discount should apply only once - check from DB

        //discount < 100%
        return !(Double.parseDouble(discount) >= 100);
    }

    private void calculateProductsCostDetails(){
        total.set(invoiceInProgress.getValue().getProducts().stream()
                .mapToDouble(value -> value.getTotalAmount().getValue()).sum());
        totalUSD.set(invoiceInProgress.getValue().getProducts().stream()
                .mapToDouble(value -> convertToDollar(value.getTotalAmount().getValue()).getValue()).sum());
        subtotal.set(invoiceInProgress.getValue().getProducts().stream().mapToDouble(value -> {
            Double discountAmount = value.priceProperty().multiply(value.getCurrentDiscount()).divide(100).getValue();
            return value.priceProperty().getValue() - discountAmount;
        }).sum());
        totalVat.set(invoiceInProgress.getValue().getProducts().stream()
                .mapToDouble(value -> value.getVatAmount().getValue()).sum());
    }
}
