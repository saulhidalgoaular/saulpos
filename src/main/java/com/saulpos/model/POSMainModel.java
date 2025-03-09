package com.saulpos.model;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.bean.*;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.exception.SaulPosException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class POSMainModel extends AbstractModel{

    private UserB userB;
    private Assignment assignment;
    private SimpleObjectProperty<Invoice> invoiceInProgress = new SimpleObjectProperty<>();
    private SimpleObjectProperty<InvoiceDetail> selectedInvoiceDetail = new SimpleObjectProperty<>();

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private SimpleStringProperty clockValue = new SimpleStringProperty();
    private SimpleStringProperty dateValue = new SimpleStringProperty();

    private SimpleStringProperty employeeName = new SimpleStringProperty();

    private ObservableList<Invoice> invoiceWaiting = FXCollections.observableArrayList();

    private SimpleStringProperty barcodeBar = new SimpleStringProperty();

    private SimpleObjectProperty<DollarRate> enabledDollarRate = new SimpleObjectProperty<>();

    private SimpleStringProperty cashierName = new SimpleStringProperty();

    private SimpleBooleanProperty clientPanelVisible = new SimpleBooleanProperty();

    public POSMainModel(UserB userB, Assignment assignment) throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        this.userB = userB;
        this.assignment = assignment;
        initialize();

        initializeEnabledDollarRate();

        Invoice invoice = new Invoice(getEnabledDollarRate().getExchangeRatePerDollar());
        invoice.saveOrUpdate();
        invoiceInProgress.set(invoice);
    }

    @Override
    public void addChangedListeners() {
        // Think where to add the bindings. If we add them in the bean, it
        // might bring inconsistencies while we load it from database.

    }


    @Override
    public void addListeners() {

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

        employeeName.set("Cashier: " + userB.getName() + " " + userB.getLastname());
        cashierName.set("Cash: " + assignment.getCashier().getDescription());
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

    public SimpleObjectProperty<Invoice> invoiceInProgressProperty() {
        return invoiceInProgress;
    }

    public DollarRate getEnabledDollarRate() {
        return enabledDollarRate.get();
    }

    public SimpleObjectProperty<DollarRate> enabledDollarRateProperty() {
        return enabledDollarRate;
    }

    public void setEnabledDollarRate(DollarRate enabledDollarRate) {
        this.enabledDollarRate.set(enabledDollarRate);
    }

    public String getCashierName() {
        return cashierName.get();
    }

    public SimpleStringProperty cashierNameProperty() {
        return cashierName;
    }

    public void setCashierName(String cashierName) {
        this.cashierName.set(cashierName);
    }

    public boolean isClientPanelVisible() {
        return clientPanelVisible.get();
    }

    public SimpleBooleanProperty clientPanelVisibleProperty() {
        return clientPanelVisible;
    }

    public void setClientPanelVisible(boolean clientPanelVisible) {
        this.clientPanelVisible.set(clientPanelVisible);
    }

    public InvoiceDetail getSelectedInvoiceDetail() {
        return selectedInvoiceDetail.get();
    }

    public SimpleObjectProperty<InvoiceDetail> selectedInvoiceDetailProperty() {
        return selectedInvoiceDetail;
    }

    public void setSelectedInvoiceDetail(InvoiceDetail selectedInvoiceDetail) {
        this.selectedInvoiceDetail.set(selectedInvoiceDetail);
    }

    public void addItemToInvoice() throws Exception {
        Product product = new Product();
        product.setBarcode(barcodeBar.getValue());
        final List<Product> list = DatabaseConnection.getInstance().listBySample(Product.class, product, AbstractDataProvider.SearchType.EQUAL);
        if (list.size() == 1) {
            if (list.getFirst().getExistence() > 0){
                Product productToAdd = list.getFirst();

                InvoiceDetail invoiceDetail = addProductToInvoiceDetails(productToAdd);
                invoiceDetail.saveOrUpdate();
                productToAdd.setExistence(productToAdd.getExistence() - 1);
                productToAdd.saveOrUpdate();

                invoiceInProgress.get().saveOrUpdate();
                barcodeBar.setValue("");
                System.out.println("Invoice Details size: " + invoiceInProgress.get().getInvoiceDetails().size());
            }else {
                //If there is a product barcode but no existence
                DialogBuilder.createError("Error", "SAUL POS", "No existence of the scanned product").showAndWait();
                barcodeBar.setValue("");
            }
        }
    }

    private InvoiceDetail addProductToInvoiceDetails(Product productToAdd) {
        InvoiceDetail invoiceDetail = new InvoiceDetail();
        invoiceDetail.setInvoice(invoiceInProgress.get());
        invoiceDetail.setProduct(productToAdd);
        invoiceDetail.setSalePrice(productToAdd.priceProperty().get());
        invoiceDetail.setAmount(1);
        invoiceDetail.setDiscount(productToAdd.getCurrentDiscount().get());
        invoiceDetail.setCancelled(0);
        invoiceDetail.setCreationTime(LocalDateTime.now());
        invoiceInProgress.get().addInvoiceDetail(invoiceDetail);
        return invoiceDetail;
    }

    public void removeSelectedItem() throws Exception {
        InvoiceDetail removedProduct = selectedInvoiceDetail.get();
        removedProduct.getProduct().setExistence(removedProduct.getProduct().getExistence() + removedProduct.getAmount());
        invoiceInProgress.get().getObservableInvoiceDetails().remove(removedProduct);
        removedProduct.saveOrUpdate();
        invoiceInProgress.get().removeInvoiceDetail(removedProduct);
        invoiceInProgress.get().saveOrUpdate();
        System.out.println("Invoice Details size: " + invoiceInProgress.get().getInvoiceDetails().size());
    }

    private void initializeEnabledDollarRate() {
        //find from db and set the enabled dollar rate in model constructor
        DollarRate dummyRate = new DollarRate();
        dummyRate.setLocalCurrencyName("Invalid rate");
        dummyRate.setExchangeRatePerDollar(0);
        try {
            DollarRate enabledRate = findActiveDollarRate();
            if (enabledRate != null) {
                setEnabledDollarRate(enabledRate);
            }else {
                DialogBuilder.createWarning("Warning", "SAUL POS", "Could not find any enabled currency rate (set as 0)!!!").showAndWait();
                setEnabledDollarRate(dummyRate);
            }
        } catch (Exception e) {
            DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
        }
    }

    private DollarRate findActiveDollarRate() throws Exception {
        // Consider sending this to the DatabaseConnection layer.
        EntityManagerFactory entityManagerFactory = DatabaseConnection.getInstance().entityManagerFactory;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DollarRate> criteriaQuery = criteriaBuilder.createQuery(DollarRate.class);
        Root<DollarRate> root = criteriaQuery.from(DollarRate.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("enabled"), true));
        DollarRate result = entityManager.createQuery(criteriaQuery).getSingleResultOrNull();
        entityManager.close();
        return result;
    }

    public void invoiceInProgressToWaiting() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        // Return if there is no product
        if (invoiceInProgress.get().getInvoiceDetails().isEmpty()){
            DialogBuilder.createError("Error!", "SAUL POS",
                    "There is no product in product list").showAndWait();
            return;
        }

        invoiceInProgress.get().setStatus(Invoice.InvoiceStatus.Waiting);
        invoiceWaiting.add(invoiceInProgress.getValue());
        Invoice invoice = new Invoice(getEnabledDollarRate().getExchangeRatePerDollar());
        invoice.saveOrUpdate();
        invoiceInProgress.set(invoice);

        DialogBuilder.createInformation("Success!", "SAUL POS",
                "Current invoice moved into waiting state").showAndWait();
    }

    public void invoiceWaitingToInProgress(){
        System.out.println("Restore waiting invoice from waiting list.");
        // Return if there is no invoice in waiting state
        if(getInvoiceWaiting().isEmpty()){
            DialogBuilder.createInformation("Info!", "SAUL POS", "No invoice in waiting state.").showAndWait();
        }else {
            showInvoicesInWaitingState();
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
                    getInvoiceInProgress().setGlobalDiscount(discount);
                    DialogBuilder.createInformation("Info!", "SAUL POS", "Discount implemented successfully.").showAndWait();
                }else{
                    DialogBuilder.createError("Error!", "SAUL POS", "Invalid Credentials or discount!").showAndWait();
                }
            }
        });
    }
    private void showInvoicesInWaitingState() {
        ObservableList<Invoice> invoices = getInvoiceWaiting();
        TableView<Invoice> invoiceInWaitingTableView = new TableView<>();
        TableColumn<Invoice, Number> indexCol = new TableColumn<Invoice, Number>("#");
        indexCol.setSortable(false);
        indexCol.setCellValueFactory(column-> new ReadOnlyObjectWrapper<Number>(invoiceInWaitingTableView.getItems().indexOf(column.getValue()) + 1));
        TableColumn<Invoice, String> dateCol = new TableColumn<>("Created DateTime");
        dateCol.setSortable(false);
        dateCol.setCellValueFactory(column -> new ReadOnlyObjectWrapper<>(column.getValue().getCreationDate().format(DateTimeFormatter.ofPattern("HH:mm:ss MMM-dd"))));
        TableColumn<Invoice, String> clientNameCol = new TableColumn<>("Client Name");
        clientNameCol.setSortable(false);
        clientNameCol.setCellValueFactory(column ->{
            if(column.getValue().getClient() != null
                && column.getValue().getClient().getName() != null
                && !column.getValue().getClient().getName().isEmpty()
                && !column.getValue().getClient().getName().isBlank()){
                return new ReadOnlyObjectWrapper<>(column.getValue().getClient().getName());
            }else {
                return new ReadOnlyObjectWrapper<>("---");
            }
        });
        invoiceInWaitingTableView.getColumns().addAll(indexCol, dateCol, clientNameCol);
        invoiceInWaitingTableView.getItems().addAll(invoices);
        // Create an Alert
        Alert alert = new Alert(Alert.AlertType.NONE);
        alert.setTitle("Saul POS");
        alert.setHeaderText("List of Waiting Invoices");
        alert.getDialogPane().setContent(invoiceInWaitingTableView);
        alert.setWidth(300);
        alert.setHeight(400);
        ButtonType btnApply = ButtonType.APPLY;
        alert.getButtonTypes().addAll(ButtonType.CANCEL);

        // Add Apply button if any item is selected.
        invoiceInWaitingTableView.getSelectionModel().selectedItemProperty().addListener((observableValue, oldSelection, newSelection) -> {
            if(observableValue.getValue() != null && !alert.getButtonTypes().contains(btnApply)){
                alert.getButtonTypes().add(btnApply);
            }
        });

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.APPLY) {
                Invoice selectedItem = invoiceInWaitingTableView.getSelectionModel().getSelectedItem();
                if(selectedItem != null){
                    try {
                        restoreInvoice(selectedItem);
                    } catch (SaulPosException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }

    private void restoreInvoice(Invoice waitingInvoice) throws SaulPosException {
        if (!getInvoiceInProgress().getInvoiceDetails().isEmpty()){
            throw new SaulPosException("There is an invoice in progress");
        }

        waitingInvoice.setStatus(Invoice.InvoiceStatus.InProgress);

        // Remove the selected invoice from the waiting invoice list & show info dialog.
        getInvoiceWaiting().remove(waitingInvoice);
        setInvoiceInProgress(waitingInvoice);

        // System.out.println("After restoring a invoice, Waiting invoice list size: " + getInvoiceWaiting().size());
        DialogBuilder.createInformation("Success!", "SAUL POS",
                "Selected invoice is restored from waiting state!").showAndWait();
    }

    private boolean validateGlobalDiscount(String username, String password, String discount) {
        //Fixme -- Implement this method based on requirements
        // discount should apply only once - check from DB

        //discount < 100%
        return !(Double.parseDouble(discount) >= 100);
    }
}
