package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.model.Function;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.sample.Product;
import com.saulpos.javafxcrudgenerator.view.CustomButton;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.javafxcrudgenerator.view.NodeConstructor;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.*;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.Utils;
import com.saulpos.view.menu.CheckBoxTreeItemMenuGenerator;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ManageProfileMenuAction extends CrudMenuAction{

    private Utils.ViewDef viewDef;

    public ManageProfileMenuAction() {
        super(Profile.class);
    }

    @Override
    public Object run(MainModel mainModel, Pane mainPane) throws Exception {
        CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();
        crudGeneratorParameter.setClazz(this.crudClass);
        HibernateDataProvider dataProvider = new HibernateDataProvider();

        NodeConstructor customButtonConstructor = new NodeConstructor() {
            @Override
            public Node generateNode(Object... name) {
                Button customButton = new Button();
                Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.BOOK, crudGeneratorParameter.translate("permissions"), "20px", "10px", ContentDisplay.LEFT);
                customButton.setGraphic(icon);
                customButton.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                return customButton;
            }
        };

        Function customButtonFunction = new Function() {
            @Override
            public Object[] run(Object[] params) throws Exception {
                Profile profile = (Profile)params[0];
                profile.fillMissingPermissions();
                Label label = new Label("Select the permissions required for this profile:");

                VBox vBox = new VBox(5);
                vBox.setPadding(new Insets(30));
                TreeView<MenuModel> treeView = CheckBoxTreeItemMenuGenerator.generateMenuNode(profile.getSortedPermissions());

                HBox buttonsHBox = new HBox(20);
                Button closeButton = (Button) crudGeneratorParameter.getGenericButtonConstructor().generateNode("Close", FontAwesomeIcon.REMOVE);
                closeButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        try {
                            if (viewDef != null) {
                                Utils.goForward(viewDef, mainPane);
                            }
                        } catch (IOException e) {
                            DialogBuilder.createExceptionDialog("Error", "Error changing to the profile window", e.getMessage(), e).showAndWait();
                        }
                    }
                });

                Button applyButton = (Button) crudGeneratorParameter.getGenericButtonConstructor().generateNode("Apply", FontAwesomeIcon.SAVE);
                applyButton.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        try {
                            profile.saveOrUpdate();
                        } catch (Exception e) {
                            DialogBuilder.createExceptionDialog("Error", "Error saving the profile configuration", e.getMessage(), e).showAndWait();
                        }
                    }
                });

                buttonsHBox.getChildren().addAll(closeButton, applyButton);

                vBox.getChildren().addAll(label, treeView, buttonsHBox);

                Utils.goForward(new Utils.ViewDef(vBox), mainPane);
                return null;
            }
        };

        crudGeneratorParameter.addCustomButton(new CustomButton(customButtonConstructor, customButtonFunction, true));
        crudGeneratorParameter.setDataProvider(dataProvider);
        CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        CrudPresenter crud = crudGenerator.generate();
        viewDef = new Utils.ViewDef(crud.getView().getMainView());
        Utils.goForward(viewDef, mainPane);
        return null;
    }
}
