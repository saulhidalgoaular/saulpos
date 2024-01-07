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

public class POSMainPresenter extends AbstractPresenter<POSMainModel, POSMainView> {

    // Create the FXML components to bind
    @FXML
    public VBox mainPOSVBox;

    @FXML
    private TableColumn<?, ?> amountColumn;

    @FXML
    private TextField barcodeTextField;

    @FXML
    private Button chargeButton;

    @FXML
    private Button clientsButton;

    @FXML
    private Label clockLabel;

    @FXML
    private Button creditNoteButton;

    @FXML
    private Button deleteAllButton;

    @FXML
    private Button deleteCurrentButton;

    @FXML
    private TableColumn<?, ?> descriptionColumn;

    @FXML
    private TableColumn<?, ?> discountLabel;

    @FXML
    private Button exitButton;

    @FXML
    private Button globalDiscountButton;

    @FXML
    private TableView<?> itemsTableView;

    @FXML
    private Label ivaLabel;

    @FXML
    private TableColumn<?, ?> priceColumn;

    @FXML
    private Button removeCashButton;

    @FXML
    private Button sendToWaitButton;

    @FXML
    private Label subtotalLabel;

    @FXML
    private TableColumn<?, ?> totalColumn;

    @FXML
    private Label totalDollarLabel;

    @FXML
    private Label totalLabel;

    @FXML
    private TableColumn<?, ?> totalUSDColumn;

    @FXML
    private TableColumn<?, ?> vatColumn;

    @FXML
    private Button viewWaitingButton;

    @FXML
    private Button xReportButton;

    @FXML
    private Button zReportButton;

    public POSMainPresenter(POSMainModel model, POSMainView view) {
        super(model, view);
    }

    @Override
    public void addBinding() {

    }

    @Override
    public void addComponents() {

    }

    @Override
    public void initializeComponents() {

    }

    @Override
    public void entryActions() {

    }
}
