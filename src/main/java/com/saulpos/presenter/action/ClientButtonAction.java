package com.saulpos.presenter.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.model.Function;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.view.CustomButton;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.javafxcrudgenerator.view.NodeConstructor;
import com.saulpos.model.POSMainModel;
import com.saulpos.model.bean.Client;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.AbstractView;
import com.saulpos.view.POSMainView;
import com.saulpos.view.Utils;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class ClientButtonAction {
    private AbstractView viewDef;
    private CrudPresenter<Client> crudPresenter;
    public ClientButtonAction() {
    }

    public void generateCrudView(Pane mainPane, POSMainModel posMainModel, GridPane clientInfoGrid) throws Exception {
        CrudGeneratorParameter<Client> crudGeneratorParameter = new CrudGeneratorParameter<>();
        crudGeneratorParameter.setClazz(Client.class);
        HibernateDataProvider dataProvider = new HibernateDataProvider();
        crudGeneratorParameter.setDataProvider(dataProvider);

        Label label = new Label("Select customer for this invoice:");
        VBox vBox = new VBox(5);
        vBox.setPadding(new Insets(30));

        NodeConstructor addCustomerToInvoiceBtnConstructor = new NodeConstructor() {
            @Override
            public Node generateNode(Object... objects) {
                Button customAddCustomerButton = new Button();
                Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.USER_PLUS, crudGeneratorParameter.translate("Add To Invoice"), "20px", "10px", ContentDisplay.LEFT);
                customAddCustomerButton.setGraphic(icon);
                customAddCustomerButton.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                return customAddCustomerButton;
            }
        };
        Function addCustomerToInvoiceBtnFunction = new Function() {
            @Override
            public Object[] run(Object[] objects) throws Exception {
                if(viewDef != null){
                    Client selectedClient = (Client) crudPresenter.getView().getTableView().getSelectionModel().getSelectedItem();
                    if(selectedClient != null){
                        clientInfoGrid.setVisible(true);
                        ((Label) clientInfoGrid.getChildren().get(1)).setText(selectedClient.getName());
                        ((Label) clientInfoGrid.getChildren().get(3)).setText(selectedClient.getAddress());
                        ((Label) clientInfoGrid.getChildren().get(5)).setText(selectedClient.getPhone());
                        posMainModel.getInvoiceInProgress().setClient(selectedClient);
//                        System.out.println("Add this customer to invoice: " + selectedClient.getId()+" - " +selectedClient.getName());
                    }
                    Utils.goTo(new POSMainView(mainPane), (Pane) viewDef.getRootNode().getParent(), viewDef.getRootNode().getParent().getScene().getWidth() * (-1), .0, true);
                }
                return null;
            }
        };

        NodeConstructor backButtonConstructor = new NodeConstructor() {
            @Override
            public Node generateNode(Object... objects) {
                Button customBackButton = new Button();
                Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.BACKWARD, crudGeneratorParameter.translate("Back"), "20px", "10px", ContentDisplay.LEFT);
                customBackButton.setGraphic(icon);
                customBackButton.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                return customBackButton;
            }
        };
        Function backButtonFunction = new Function() {
            @Override
            public Object[] run(Object[] objects) throws Exception {
                if(viewDef != null){
                    Utils.goTo(new POSMainView(mainPane), (Pane) viewDef.getRootNode().getParent(), viewDef.getRootNode().getParent().getScene().getWidth() * (-1), .0, true);
                }
                return null;
            }
        };

        crudGeneratorParameter.addCustomButton(new CustomButton(addCustomerToInvoiceBtnConstructor, addCustomerToInvoiceBtnFunction, true));
        crudGeneratorParameter.addCustomButton(new CustomButton(backButtonConstructor, backButtonFunction, false));
        CrudGenerator<Client> crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        crudPresenter = crudGenerator.generate();
        ((Button)crudPresenter.getView().getDeleteButton()).setOnAction(actionEvent -> {
            Client selectedClient = (Client) crudPresenter.getView().getTableView().getSelectionModel().getSelectedItem();
            if(viewDef != null && selectedClient != null){
                try {
                    crudPresenter.getView().getTableView().getItems().remove(selectedClient);
                    selectedClient.delete();
                    if(posMainModel.getInvoiceInProgress().getClient() != null && posMainModel.getInvoiceInProgress().getClient().getId() == selectedClient.getId()){
                        posMainModel.getInvoiceInProgress().setClient(null);
                        clientInfoGrid.setVisible(false);
                        DialogBuilder.createWarning("Warning", "SAUL POS", "Deleted Client was attached with current invoice.").showAndWait();
                    }
                } catch (Exception e) {
                    DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
                }
            }
        });
        vBox.getChildren().addAll(label, crudPresenter.getView().getMainView());
        viewDef = new AbstractView(vBox);
        Utils.goForward(viewDef, mainPane);
    }
}
