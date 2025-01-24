package com.saulpos.presenter;

import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.LoginModel;
import com.saulpos.model.POSMainModel;
import com.saulpos.model.bean.DollarRate;
import com.saulpos.model.bean.InvoiceDetail;
import com.saulpos.model.bean.Product;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.model.printer.SoutPrinter;
import com.saulpos.presenter.action.ClientButtonAction;
import com.saulpos.view.*;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

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
    public TableView<InvoiceDetail> itemsTableView;
    @FXML
    public TableColumn<InvoiceDetail, String> descriptionColumn;
    @FXML
    public TableColumn<InvoiceDetail, Integer> amountColumn;
    @FXML
    public TableColumn<InvoiceDetail, String> discountLabel;
    @FXML
    public TableColumn<InvoiceDetail, Double> priceColumn;
    @FXML
    public TableColumn<InvoiceDetail, Double> vatColumn;
    @FXML
    public TableColumn<InvoiceDetail, Double> totalColumn;
    @FXML
    public TableColumn<InvoiceDetail, Double> totalUSDColumn;


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
    @FXML
    public Label exchangeRateLabel;
    @FXML
    public Label cashierLabel;

    @FXML
    public Label clientName;

    @FXML
    public Label clientAddress;

    @FXML
    public Label clientPhone;

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
        cashierLabel.textProperty().bind(model.cashierNameProperty());
        Bindings.bindBidirectional(barcodeTextField.textProperty(), model.barcodeBarProperty());
        Bindings.bindContentBidirectional(itemsTableView.getItems(), model.getInvoiceInProgress().getObservableInvoiceDetails());

        totalLabel.textProperty().bind(model.totalProperty().asString("%.2f"));
        subtotalLabel.textProperty().bind(model.subtotalProperty().asString("%.2f"));
        vatLabel.textProperty().bind(model.totalVatProperty().asString("%.2f"));
        totalDollarLabel.textProperty().bind(model.totalUSDProperty().asString("%.4f"));

        clientName.textProperty().bind(
                Bindings.createStringBinding(
                        () -> model.getInvoiceInProgress().clientProperty().getValue() != null
                                ? model.getInvoiceInProgress().clientProperty().getValue().getName()
                                : "",
                        model.getInvoiceInProgress().clientProperty()
                )
        );

        clientAddress.textProperty().bind(
                Bindings.createStringBinding(
                        () -> model.getInvoiceInProgress().clientProperty().getValue() != null
                                ? model.getInvoiceInProgress().clientProperty().getValue().getAddress()
                                : "",
                        model.getInvoiceInProgress().clientProperty()
                )
        );

        clientPhone.textProperty().bind(
                Bindings.createStringBinding(
                        () -> model.getInvoiceInProgress().clientProperty().getValue() != null
                                ? model.getInvoiceInProgress().clientProperty().getValue().getPhone()
                                : "",
                        model.getInvoiceInProgress().clientProperty()
                )
        );

        clientInfoGrid.visibleProperty().bind(model.clientPanelVisibleProperty());
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
            try {
                addInvoiceInWaitingState();
            } catch (PropertyVetoException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            } catch (URISyntaxException ex) {
                throw new RuntimeException(ex);
            } catch (ClassNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
        viewWaitingButton.setOnAction(e->{
            restoreWaitingInvoice();
        });
        removeCashButton.setOnAction(e->{
            extractMoney();
        });
        globalDiscountButton.setOnAction(e->{
            setGlobalDiscount();
        });
        chargeButton.setOnAction(e->{
            proceedToPayment();
        });

        descriptionColumn.setCellValueFactory(cell -> cell.getValue().getProduct().descriptionProperty());
        priceColumn.setCellValueFactory(cell -> cell.getValue().getProduct().priceProperty().asObject());
        amountColumn.setCellValueFactory(cell -> cell.getValue().amountProperty().asObject());
        discountLabel.setCellValueFactory(cell -> cell.getValue().getProduct().getCurrentDiscountString());
        vatColumn.setCellValueFactory(cell ->{
            String str = cell.getValue().getProduct().getVatAmount().asString("%.3f").get();
            return new SimpleDoubleProperty(Double.parseDouble(str)).asObject();
        });
        totalColumn.setCellValueFactory(cell -> {
            String str = cell.getValue().getProduct().getTotalAmount().asString("%.3f").get();
            return new SimpleDoubleProperty(Double.parseDouble(str)).asObject();
        });
        totalUSDColumn.setCellValueFactory(cell -> {
            String str = model.convertToDollar(cell.getValue().getProduct().getTotalAmount().getValue()).asString("%.4f").get();
            return new SimpleDoubleProperty(Double.parseDouble(str)).asObject();
        });
        showExchangeRate();

        // For this we need to keep the local currency rate in dollars somewhere.
        // Create another bean that stores the currency rate.

    }


    @Override
    public void entryActions() {

    }
    public void handleKeyReleased(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        if(model != null){
            switch (keyCode) {
                case F1 -> {
                    // Should open the pay window for payment.
                    System.out.println("Se presionó F1 (Cobrar)");
                    proceedToPayment();
                }
                case BACK_SPACE -> {
                    // should delete items from item table view
                    if(itemsTableView.isFocused() && !itemsTableView.getItems().isEmpty()
                            && itemsTableView.getSelectionModel().getSelectedIndex() > -1){
                        try {
                            model.removeItem(itemsTableView);
                            System.out.println("Invoice product list after deletion: " +
                                    model.invoiceInProgressProperty().getValue().getInvoiceDetails().size());
                        }catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
                case F2 -> {
                    System.out.println("Se presionó F2 (Clientes)");
                    addClient();
                }
                case F3 -> {
                    System.out.println("Se presionó F3 (Extraer dinero)");
                    extractMoney();
                }
                case F4 -> {
                    System.out.println("Se presionó F4 (A espera)");
                    try {
                        addInvoiceInWaitingState();
                    } catch (PropertyVetoException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                case F5 -> {
                    System.out.println("Se presionó F5 (Ver espera)");
                    restoreWaitingInvoice();
                }
                case DELETE -> {System.out.println("Se presionó DEL (Borrar pedido)");}
                case F6 -> {System.out.println("Se presionó F6 (Nota de credito)");}
                case F7 -> {
                    System.out.println("Se presionó F7 (Descuento Global)");
                    setGlobalDiscount();
                }
                case ESCAPE -> {
                    System.out.println("Se presionó ESC (Salir)");
                    logout();
                }
                case F8 -> {
                    System.out.println("Se presionó F8 (Reporte X)");
                    // test the invoice printing result
                    // need to save the invoice in DB
                    SoutPrinter printer = new SoutPrinter();
                    model.getInvoiceInProgress().setTotalWithoutVat(model.getSubtotal());
                    model.getInvoiceInProgress().setTotalWithVat(model.getTotal());
                    model.getInvoiceInProgress().setVat(model.getTotalVat());
                    model.getInvoiceInProgress().setTotalInUSD(model.getTotalUSD());
                    printer.printInvoice(model.getInvoiceInProgress());
                }
                case END -> {System.out.println("Se presionó END (Reporte Z)");}
                case ENTER -> {
                    try {
                        if(barcodeTextField.getText() != null && !barcodeTextField.getText().isEmpty()){
                            if(model.getEnabledDollarRate() == null){
                                DollarRate dollarRate = model.getEnabledDollarRate();
                                if(dollarRate != null){
                                    model.setEnabledDollarRate(dollarRate);
                                }
                            }
                            if(model.getInvoiceInProgress().getCreationDate() == null){
                                model.getInvoiceInProgress().setCreationDate(LocalDateTime.now());
                            }
                            model.addItem();

                            System.out.println("Invoice product list after add: " +
                                    model.invoiceInProgressProperty().getValue().getInvoiceDetails().size());
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
            parentPane.getChildren().removeFirst();
            removeModelFromPresenter();
            Utils.goForward(loginView, parentPane);
        } catch (Exception e){
            DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
        }
    }

    private void addClient(){
        ClientButtonAction clientButton = new ClientButtonAction();
        try {
            clientButton.generateCrudView(mainPOSVBox, model);
        } catch (Exception e) {
            DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
        }
    }

    private void addInvoiceInWaitingState() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        model.invoiceInProgressToWaiting();
    }

    private void restoreWaitingInvoice(){
        model.invoiceWaitingToInProgress();
    }

    private void extractMoney(){
        model.transferMoney();
    }

    private void setGlobalDiscount(){
        System.out.println("Apply global discount.");
        if(!itemsTableView.getItems().isEmpty()){
            model.applyGlobalDiscount();
        }else{
            DialogBuilder.createWarning("Warning!", "SAUL POS", "No product in current invoice!").showAndWait();
        }
    }

    private void showExchangeRate() {
        //show the currency exchange rate when POS window opens.
//        System.out.println("Show exchange rate name: " + model.getEnabledDollarRate().getLocalCurrencyName()+
//                " -rate:" + model.getEnabledDollarRate().getExchangeRatePerDollar());
        if (model.getEnabledDollarRate().getExchangeRatePerDollar() > 0f) {
            exchangeRateLabel.setText(model.getLanguage().getString("exchangeRate")+" ("+
                    model.getEnabledDollarRate().getExchangeRatePerDollar()+ " " +
                    model.getEnabledDollarRate().getLocalCurrencyName()+
                    "/$)");
        }else {

            exchangeRateLabel.setText(model.getLanguage().getString("exchangeRate.invalid"));
//            exchangeRateLabel.setText("Exchange rate: (Invalid)");
        }
    }

    private void proceedToPayment() {
        if(itemsTableView.getItems().size() == 0){
            DialogBuilder.createInformation("Info!", "SAUL POS",
                    "There is no product in product list").showAndWait();
            return;
        }
        // Open the pay window.
        AbstractView viewDef;
        try {
            VBox paymentPane = new VBox();
            Button cashPayment = GlyphsDude.createIconButton(FontAwesomeIcon.DOLLAR, "Cash", "20px", "18px", ContentDisplay.TOP);
            cashPayment.setPrefSize(250, 100);
            Button visaCardPayment = GlyphsDude.createIconButton(FontAwesomeIcon.CC_VISA, "Visa Card", "20px", "18px", ContentDisplay.TOP);
            visaCardPayment.setPrefSize(250, 100);
            Button masterCardPayment = GlyphsDude.createIconButton(FontAwesomeIcon.CC_MASTERCARD, "Master Card", "20px", "18px", ContentDisplay.TOP);
            masterCardPayment.setPrefSize(250, 100);
            Button splitPayment = GlyphsDude.createIconButton(FontAwesomeIcon.DEVIANTART, "Split", "20px", "18px", ContentDisplay.TOP);;
            splitPayment.setPrefSize(250, 100);
            Button backButton = GlyphsDude.createIconButton(FontAwesomeIcon.BACKWARD, "Back", "20px", "18px", ContentDisplay.LEFT);
            backButton.setPrefSize(200, 50);

            HBox row1 = new HBox(30, cashPayment, splitPayment);
            row1.setAlignment(Pos.CENTER);
            HBox row2 = new HBox(30, visaCardPayment, masterCardPayment);
            row2.setAlignment(Pos.CENTER);
            HBox row3 = new HBox(backButton);
            row3.setAlignment(Pos.CENTER);

            Label label = new Label("Payment Options");
            label.setFont(Font.font("System", FontWeight.BOLD,32));
            Label dollarLabel = new Label("Total $ " + totalDollarLabel.getText());
            dollarLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            Border border = new Border(new BorderStroke(
                    Color.color(.12,.34,.65),
                    BorderStrokeStyle.DASHED,
                    new CornerRadii(3),
                    new BorderWidths(1)
            ));
            dollarLabel.setBorder(border);
            dollarLabel.setPadding(new Insets(3));
            paymentPane.getChildren().addAll(label,dollarLabel, row1, row2, row3);
            paymentPane.setAlignment(Pos.CENTER);
            paymentPane.setSpacing(30);

            viewDef = new AbstractView(paymentPane);
            backButton.setOnAction(e->{
                try {
                    Utils.goBackRemove(new POSMainView(mainPOSVBox), null, (Pane) viewDef.getRootNode());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            });
            // Utils.goForward(destinationReference, sourceReference);
            Utils.goForward(viewDef, mainPOSVBox);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}