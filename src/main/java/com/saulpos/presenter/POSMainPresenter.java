package com.saulpos.presenter;

import com.saulpos.model.POSMainModel;
import com.saulpos.view.POSMainView;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
//
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import com.saulpos.view.POSIcons;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.model.bean.Product;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.List;
import javafx.scene.control.cell.PropertyValueFactory;

//


public class POSMainPresenter extends AbstractPresenter<POSMainModel, POSMainView> {



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
    public Button viewWaitingButton;

    @FXML
    public Button xReportButton;

    @FXML
    public Button zReportButton;

    //Aqui
    private final HibernateDataProvider hibernateDataProvider;
    public POSMainPresenter(POSMainModel model, POSMainView view) {
        super(model, view);
        this.hibernateDataProvider = new HibernateDataProvider();

    }

    @Override
    public void addBinding() {

    }

    @Override
    public void addComponents() {

    }

    @Override
    public void initializeComponents() {
        setButtonsIcons("MONEY",chargeButton);
        setButtonsIcons("MINUS_CIRCLE",deleteCurrentButton);
        setButtonsIcons("USER",clientsButton);
        setButtonsIcons("HAND_PAPER_ALT",removeCashButton);
        setButtonsIcons("CLOCK_ALT",sendToWaitButton);
        setButtonsIcons("EYE",viewWaitingButton);
        setButtonsIcons("TRASH",deleteAllButton);
        setButtonsIcons("FILE_TEXT",creditNoteButton);
        setButtonsIcons("USD",globalDiscountButton);
        setButtonsIcons("SIGN_OUT",exitButton);
        setButtonsIcons("BAR_CHART",xReportButton);
        setButtonsIcons("BAR_CHART",zReportButton);

        Platform.runLater(() -> {
            // focus on barcode text.
            barcodeTextField.requestFocus();
            //Get scene for keyreleased event
            Scene principalscene = barcodeTextField.getScene();
            principalscene.setOnKeyReleased(this::handleKeyReleased);



        });

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
        }
    }
    public void setButtonsIcons(String iconname, Button buttonname ){
        Label iconLabel = POSIcons.getGraphic(iconname);
        buttonname.setGraphic(iconLabel);
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
