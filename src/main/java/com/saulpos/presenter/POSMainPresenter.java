package com.saulpos.presenter;

import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.LoginModel;
import com.saulpos.model.POSMainModel;
import com.saulpos.model.bean.Client;
import com.saulpos.model.bean.DollarRate;
import com.saulpos.model.bean.Invoice;
import com.saulpos.model.bean.Product;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.presenter.action.ClientButtonAction;
import com.saulpos.view.LoginView;
import com.saulpos.view.ParentPane;
import com.saulpos.view.Utils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

//


public class POSMainPresenter extends AbstractPresenter<POSMainModel> {



    // Create the FXML components to bind
    @FXML
    public VBox mainPOSVBox;

    @FXML
    public TextField barcodeTextField;

    @FXML
    public Button chargeButton;

    @FXML
    public Button clientsButton;

    @FXML
    public Label clockLabel;

    @FXML
    public Label dateLabel;

    @FXML
    public Button creditNoteButton;

    @FXML
    public Button deleteAllButton;

    @FXML
    public Button deleteCurrentButton;
    @FXML
    public TableView<Product> itemsTableView;
    @FXML
    public TableColumn<Product, String> descriptionColumn;
    @FXML
    public TableColumn<Product, Integer> amountColumn;
    @FXML
    public TableColumn<Product, String> discountLabel;
    @FXML
    public TableColumn<Product, Double> priceColumn;
    @FXML
    public TableColumn<Product, Double> vatColumn;
    @FXML
    public TableColumn<Product, Double> totalColumn;
    @FXML
    public TableColumn<Product, Double> totalUSDColumn;


    @FXML
    public Button exitButton;

    @FXML
    public Button globalDiscountButton;
    @FXML
    public Label vatLabel;

    @FXML
    public Button removeCashButton;

    @FXML
    public Button sendToWaitButton;

    @FXML
    public Label subtotalLabel;

    @FXML
    public Label totalDollarLabel;

    @FXML
    public Label totalLabel;

    @FXML
    private Label employeeLabel;

    @FXML
    public Button viewWaitingButton;

    @FXML
    public Button xReportButton;

    @FXML
    public Button zReportButton;
    @FXML
    public GridPane clientInfoGrid;

    //Aqui
    private final HibernateDataProvider hibernateDataProvider;

    public POSMainPresenter(POSMainModel model) {
        super(model);
        this.hibernateDataProvider = new HibernateDataProvider();

    }

    @Override
    public void addBinding() {
        clockLabel.textProperty().bind(model.clockValueProperty());
        dateLabel.textProperty().bind(model.dateValueProperty());
        employeeLabel.textProperty().bind(model.employeeNameProperty());
        Bindings.bindBidirectional(barcodeTextField.textProperty(), model.barcodeBarProperty());
        Bindings.bindContentBidirectional(itemsTableView.getItems(), model.getInvoiceInProgress().getProducts());

        totalLabel.textProperty().bind(model.totalProperty().asString("%.2f"));
        subtotalLabel.textProperty().bind(model.subtotalProperty().asString("%.2f"));
        vatLabel.textProperty().bind(model.totalVatProperty().asString("%.2f"));
        totalDollarLabel.textProperty().bind(model.totalUSDProperty().asString("%.2f"));
    }

    @Override
    public void addComponents() {

    }

    @Override
    public void initializeComponents() {

        Platform.runLater(() -> {
            // focus on barcode text.
            barcodeTextField.requestFocus();
            //Get scene for keyreleased event
            Scene principalscene = barcodeTextField.getScene();
            principalscene.setOnKeyReleased(this::handleKeyReleased);


        });
        exitButton.setOnAction(e->{
            logout();
        });
        clientsButton.setOnAction(e->{
            addClient();
        });
        sendToWaitButton.setOnAction(e->{
            addInvoiceInWaitingState();
        });
        viewWaitingButton.setOnAction(e->{
            restoreWaitingInvoice();
        });

        descriptionColumn.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        priceColumn.setCellValueFactory(cell -> cell.getValue().getCurrentPrice().asObject());
        amountColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(1).asObject());
        discountLabel.setCellValueFactory(cell -> cell.getValue().getCurrentDiscountString());
        vatColumn.setCellValueFactory(cell ->{
            String str = cell.getValue().getVatAmount().asString("%.3f").get();
            return new SimpleDoubleProperty(Double.parseDouble(str)).asObject();
        });
        totalColumn.setCellValueFactory(cell -> {
            String str = cell.getValue().getTotalAmount().asString("%.3f").get();
            return new SimpleDoubleProperty(Double.parseDouble(str)).asObject();
        });
        totalUSDColumn.setCellValueFactory(cell -> {
            String str = model.convertToDollar(cell.getValue().getTotalAmount().getValue()).asString("%.3f").get();
            return new SimpleDoubleProperty(Double.parseDouble(str)).asObject();
        });

        // For this we need to keep the local currency rate in dollars somewhere.
        // Create another bean that stores the currency rate.

    }


    @Override
    public void entryActions() {

    }
    public void handleKeyReleased(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        switch (keyCode) {
            case F1 -> {System.out.println("Se presionó F1 (Cobrar)");}
            case BACK_SPACE -> {
                System.out.println("Se presionó BACKSPACE (Borrar)");
                // should delete items from item table view
                if(itemsTableView.isFocused() && itemsTableView.getItems().size() > 0
                    && itemsTableView.getSelectionModel().getSelectedIndex() > -1){
                    try {
                        model.removeItem(itemsTableView);
                        System.out.println("Invoice product list after deletion: " +
                                model.invoiceInProgressProperty().getValue().getProducts().size());
                    }catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            case F2 -> {
                System.out.println("Se presionó F2 (Clientes)");
                addClient();
            }
            case F3 -> {System.out.println("Se presionó F3 (Extraer dinero)");}
            case F4 -> {
                System.out.println("Se presionó F4 (A espera)");
                addInvoiceInWaitingState();
            }
            case F5 -> {
                System.out.println("Se presionó F5 (Ver espera)");
                restoreWaitingInvoice();
            }
            case DELETE -> {System.out.println("Se presionó DEL (Borrar pedido)");}
            case F6 -> {System.out.println("Se presionó F6 (Nota de credito)");}
            case F7 -> {System.out.println("Se presionó F7 (Descuento Global)");}
            case ESCAPE -> {
                System.out.println("Se presionó ESC (Salir)");
                logout();
            }
            case F8 -> {System.out.println("Se presionó F8 (Reporte X)");}
            case END -> {System.out.println("Se presionó END (Reporte Z)");}
            case ENTER -> {
                try {
                    if(barcodeTextField.getText() != null && !barcodeTextField.getText().isEmpty()){
                        if(model.getActiveDollarRate() == null){
                            DollarRate dollarRate = model.findActiveDollarRate();
                            if(dollarRate != null){
                                model.setActiveDollarRate(dollarRate);
                            }
                        }
                        model.addItem();

                        System.out.println("Invoice product list after add: " +
                                model.invoiceInProgressProperty().getValue().getProducts().size());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            case UP -> {
                moveFocus(-1);
            }
            case DOWN -> {
                moveFocus(1);
            }
        }
    }

    private void moveFocus(int delta) {
        final int currentIndex = itemsTableView.getSelectionModel().getSelectedIndex();
        if (currentIndex == -1) {
            itemsTableView.getSelectionModel().select(0);
        }
        int newIndex = currentIndex + delta;
        if (newIndex < 0) {
            newIndex = itemsTableView.getItems().size() - 1;
        } else if (newIndex >= itemsTableView.getItems().size()) {
            newIndex = 0;
        }
        itemsTableView.getSelectionModel().select(newIndex);
    }
    private void logout() {
        try {
            System.out.println("Logging out...");
            LoginModel loginModel = new LoginModel();
            LoginPresenter loginPresenter = new LoginPresenter(loginModel);
            LoginView loginView = new LoginView("/login.fxml", loginPresenter);
            ParentPane parentPane = (ParentPane) mainPOSVBox.getParent();
            parentPane.getChildren().remove(0);
            Utils.goForward(loginView, parentPane);
        } catch (Exception e){
            DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
        }
    }

    private void addClient(){
        ClientButtonAction clientButton = new ClientButtonAction();
        try {
            clientButton.generateCrudView(mainPOSVBox, model, clientInfoGrid);
        } catch (Exception e) {
            DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
        }
    }

    private void addInvoiceInWaitingState(){
        // Return if there is no product in table view.
        if(itemsTableView.getItems().size() == 0){
            DialogBuilder.createError("Error!", "SAUL POS",
                    "There is no product in product list").showAndWait();
            return;
        }
        System.out.println("Moving Current invoice in waiting state!");
        //Considering only one invoice can be in waiting state.
        if(model.getInvoiceWaiting().size() > 0){
            DialogBuilder.createError("Error!", "SAUL POS",
                    "Already an invoice in waiting state. Another invoice is not allowed to move in waiting state.").showAndWait();
        } else if (model.getInvoiceWaiting().size() == 0) {
            //Move inProgress invoice data into the waiting invoice data in model.
            Invoice waitingInvoice = new Invoice();
            waitingInvoice.setStatus(Invoice.InvoiceStatus.Waiting);

            //Move all products from inProgress invoice to waiting invoice & clear products from inProgress invoice
            ObservableList<Product> products = FXCollections.observableArrayList();
            products.addAll(model.getInvoiceInProgress().getProducts());
            waitingInvoice.setProducts(products);
            model.getInvoiceInProgress().getProducts().clear();

            //Move client from inProgress invoice to waiting invoice & set client=null in inProgress invoice.
            //And hide the clientInfoGrid
            Client inProgressClient = model.getInvoiceInProgress().getClient();
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
                model.getInvoiceInProgress().setClient(null);
                clientInfoGrid.setVisible(false);
            }
            // Adding this waitingInvoice into the model & show info dialog.
            model.getInvoiceWaiting().add(waitingInvoice);
            DialogBuilder.createInformation("Success!", "SAUL POS",
                    "Current invoice moved into waiting state!").showAndWait();
        }
    }

    private void restoreWaitingInvoice(){
        System.out.println("Restore waiting invoice from waiting list.");
        // Return if there is no invoice in waiting state
        if(model.getInvoiceWaiting().size() == 0){
            DialogBuilder.createInformation("Info!", "SAUL POS", "No invoice in waiting state.").showAndWait();
        }else if(model.getInvoiceWaiting().size() == 1){
            System.out.println("Current invoice product list size: " + model.getInvoiceWaiting().get(0).getProducts().size());
            //Clear the product list from inProgress invoice and restore products from waiting invoice.
            Invoice waitingInvoice = model.getInvoiceWaiting().getFirst();
            model.getInvoiceInProgress().getProducts().clear();
            model.getInvoiceInProgress().getProducts().addAll(waitingInvoice.getProducts());

            //Clear the client info from inProgress invoice and restore client info from waiting invoice.
            if(waitingInvoice.getClient() == null){
                model.getInvoiceInProgress().setClient(null);
                clientInfoGrid.setVisible(false);
            }else{
                model.getInvoiceInProgress().setClient(waitingInvoice.getClient());
                clientInfoGrid.setVisible(true);
                ((Label) clientInfoGrid.getChildren().get(1)).setText(model.getInvoiceInProgress().getClient().getName());
                ((Label) clientInfoGrid.getChildren().get(3)).setText(model.getInvoiceInProgress().getClient().getAddress());
                ((Label) clientInfoGrid.getChildren().get(5)).setText(model.getInvoiceInProgress().getClient().getPhone());
            }
            //Clear the waiting invoice list & show info dialog.
            model.getInvoiceWaiting().clear();
            DialogBuilder.createInformation("Success!", "SAUL POS",
                    "Current invoice is restored from waiting state!").showAndWait();
        }
    }
}