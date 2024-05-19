package com.saulpos.presenter;

import com.saulpos.model.POSMainModel;
import com.saulpos.model.bean.Product;
import com.saulpos.model.dao.HibernateDataProvider;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
    public TableColumn<Product, Double> discountLabel;
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
        Bindings.bindContent(itemsTableView.getItems(), model.getInvoiceInProgressProperty().getProducts());
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

        descriptionColumn.setCellValueFactory(cell -> cell.getValue().descriptionProperty());
        //priceColumn.setCellValueFactory(cell -> cell.getValue().priceProperty());

    }


    @Override
    public void entryActions() {

    }
    public void handleKeyReleased(KeyEvent event) {
        KeyCode keyCode = event.getCode();
        switch (keyCode) {
            case F1 -> {System.out.println("Se presionó F1 (Cobrar)");}
            case BACK_SPACE -> {System.out.println("Se presionó BACKSPACE (Borrar)");}
            case F2 -> {System.out.println("Se presionó F2 (Clientes)");}
            case F3 -> {System.out.println("Se presionó F3 (Extraer dinero)");}
            case F4 -> {System.out.println("Se presionó F4 (A espera)");}
            case F5 -> {System.out.println("Se presionó F5 (Ver espera)");}
            case DELETE -> {System.out.println("Se presionó DEL (Borrar pedido)");}
            case F6 -> {System.out.println("Se presionó F6 (Nota de credito)");}
            case F7 -> {System.out.println("Se presionó F7 (Descuento Global)");}
            case ESCAPE -> {System.out.println("Se presionó ESC (Salir)");}
            case F8 -> {System.out.println("Se presionó F8 (Reporte X)");}
            case END -> {System.out.println("Se presionó END (Reporte Z)");}
            case ENTER -> {
                try {
                    model.addItem();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
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
