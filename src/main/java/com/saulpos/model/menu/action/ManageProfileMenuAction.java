/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.model.Function;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.view.CustomButton;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.javafxcrudgenerator.view.NodeConstructor;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.Profile;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.AbstractView;
import com.saulpos.view.Utils;
import com.saulpos.view.menu.CheckBoxTreeItemMenuGenerator;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ManageProfileMenuAction extends CrudMenuAction{

    private AbstractView viewDef;

    public ManageProfileMenuAction() {
        super(Profile.class);
    }

    @Override
    public void run(MainModel mainModel, Pane mainPane) throws Exception {
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
                TreeView<MenuModel> treeView = CheckBoxTreeItemMenuGenerator.generateMenuNode(profile.getTreeSetPermissions());

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

                Utils.goForward(new AbstractView(vBox), mainPane);
                return null;
            }
        };

        crudGeneratorParameter.addCustomButton(new CustomButton(customButtonConstructor, customButtonFunction, true));
        crudGeneratorParameter.setDataProvider(dataProvider);
        CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        CrudPresenter crud = crudGenerator.generate();

        viewDef = new AbstractView(crud.getView().getMainView());
        Utils.goForward(viewDef, mainPane);
    }
}
