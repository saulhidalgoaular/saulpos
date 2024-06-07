package com.saulpos.presenter;

import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.LoginModel;
import com.saulpos.model.POSMainModel;
import com.saulpos.model.bean.Discount;
import com.saulpos.model.bean.Price;
import com.saulpos.model.bean.Product;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.LoginView;
import com.saulpos.view.ParentPane;
import com.saulpos.view.Utils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
    public Label ivaLabel;

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
    private final double vatRate = 15f;
    private enum totalType {
        SubTotal, IVA, Total, totalUSD
    }

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
        Bindings.bindContentBidirectional(itemsTableView.getItems(), model.getInvoiceInProgressProperty().getProducts());
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
        resetAllTotal();
        descriptionColumn.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        priceColumn.setCellValueFactory(cell -> {
            //Calculate price for current day.
            Set<Price> priceSet = cell.getValue().getPrice();
            for(Price price: priceSet){
                LocalDate now = LocalDate.now();
                if(now.isAfter(price.getFromDate()) && now.isBefore(price.getToDate())){
                    return new SimpleDoubleProperty(price.getPrice()).asObject();
                }
            }
            return new SimpleDoubleProperty(0f).asObject();
        });
        amountColumn.setCellValueFactory(cell -> new SimpleIntegerProperty(1).asObject());
        discountLabel.setCellValueFactory(cell ->{
            //check if discount is available till now?
            Discount discount = cell.getValue().getDiscount();
            LocalDate now = LocalDate.now();
            if(now.isAfter(discount.getStartingDate()) && now.isBefore(discount.getEndingDate())){
                double discountAmount = cell.getValue().getDiscount().getPercentage();
                return new SimpleStringProperty(String.valueOf(discountAmount) + "%");
            }else{
                return new SimpleStringProperty("0.0%");
            }

        });
        vatColumn.setCellValueFactory(cell ->{
            double price = priceColumn.getCellObservableValue(cell.getValue()).getValue();
            double vat = (price * vatRate)/100;
            return new SimpleDoubleProperty(vat).asObject();
        });
        totalColumn.setCellValueFactory(cell ->{
            //price + vat = total column
            double price = priceColumn.getCellObservableValue(cell.getValue()).getValue();
            double vat = vatColumn.getCellObservableValue(cell.getValue()).getValue();
            return new SimpleDoubleProperty(price + vat).asObject();
        });
        totalUSDColumn.setCellValueFactory(cell -> {
            // (totalColumnPrice * unit number) - discount rate
            double totalColumnPrice = totalColumn.getCellObservableValue(cell.getValue()).getValue();
            int unit = amountColumn.getCellObservableValue(cell.getValue()).getValue();
            String str = discountLabel.getCellObservableValue(cell.getValue()).getValue();
            double discountPercent = Double.parseDouble(str.substring(0, str.length()-1));
            double result = (totalColumnPrice * unit) - (((totalColumnPrice * unit) * discountPercent) / 100);
            return new SimpleDoubleProperty(result).asObject();
        });
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
                    int selectedIndex = itemsTableView.getSelectionModel().getSelectedIndex();
                    itemsTableView.getItems().remove(selectedIndex);
                    updateAllTotal();
                    System.out.println("Invoice product list after deletion: " +
                            model.invoiceInProgressPropertyProperty().getValue().getProducts().size());
                }
            }
            case F2 -> {System.out.println("Se presionó F2 (Clientes)");}
            case F3 -> {System.out.println("Se presionó F3 (Extraer dinero)");}
            case F4 -> {System.out.println("Se presionó F4 (A espera)");}
            case F5 -> {System.out.println("Se presionó F5 (Ver espera)");}
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
                    if(!barcodeTextField.getText().isEmpty()){
                        model.addItem();
                        updateAllTotal();
                        System.out.println("Invoice product list after add: " +
                                model.invoiceInProgressPropertyProperty().getValue().getProducts().size());
//                        Product p = model.getInvoiceInProgressProperty().getProducts().getLast();
//                        System.out.println("Product price: " + p.getPrice().stream().findFirst().get().getPrice());
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

    private void updateAllTotal(){
        if(itemsTableView.getItems().size() == 0){
            resetAllTotal();
        }else {
            Map<String, String> map = calculateTotalType();
            subtotalLabel.setText(map.get(totalType.SubTotal.name()));
            ivaLabel.setText(map.get(totalType.IVA.name()));
            totalLabel.setText(map.get(totalType.Total.name()));
            totalDollarLabel.setText(map.get(totalType.totalUSD.name()));
        }
    }

    private Map<String, String> calculateTotalType(){ //calculateAllTotal
        Map<String, String> totalCalculationMap = new HashMap<>();
        double subTotalResult = 0f, vatResult = 0f, totalResult = 0f, totalUSDResult = 0f;
        if(itemsTableView.getItems().size()>0){
            for(Product p: itemsTableView.getItems()){
                subTotalResult += priceColumn.getCellObservableValue(p).getValue();
                vatResult += vatColumn.getCellObservableValue(p).getValue();
                totalResult += totalColumn.getCellObservableValue(p).getValue();
                totalUSDResult += totalUSDColumn.getCellObservableValue(p).getValue();
            }
            totalCalculationMap.put(totalType.SubTotal.name(), String.valueOf(subTotalResult));
            totalCalculationMap.put(totalType.IVA.name(), String.valueOf(vatResult));
            totalCalculationMap.put(totalType.Total.name(), String.valueOf(totalResult));
            totalCalculationMap.put(totalType.totalUSD.name(), String.valueOf(totalUSDResult));
        }
        return totalCalculationMap;
    }
    private void resetAllTotal(){
        totalDollarLabel.setText("0.0");
        totalLabel.setText("0.0");
        ivaLabel.setText("0.0");
        subtotalLabel.setText("0.0");
    }

}
/*            // Obtener y mostrar la lista de products
            List<Product> products = hibernateDataProvider.getAllItems(Product.class);
            for (Product product : products) {
                System.out.println("Product ID: " + product.getId());
                System.out.println("Barcode: " + product.getBarcode());
                System.out.println("Brand: " + product.getBrand());
                System.out.println("Description: " + product.getDescription());
                // Otros campos...
                System.out.println("-------------------------");
            }
            */
