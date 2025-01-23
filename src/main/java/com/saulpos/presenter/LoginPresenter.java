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
package com.saulpos.presenter;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.view.controls.SimplePasswordControl;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.dlsc.formsfx.view.renderer.GroupRenderer;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.LoginModel;
import com.saulpos.model.MainModel;
import com.saulpos.model.POSMainModel;
import com.saulpos.model.bean.Assignment;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.UserB;
import com.saulpos.view.LoginView;
import com.saulpos.view.POSMainView;
import com.saulpos.view.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginPresenter extends AbstractPresenter<LoginModel> {
    @FXML
    public VBox mainVBox;

    private Form form;

    private Form systemForm;

    @FXML
    private VBox rightPane;

    @FXML
    private VBox leftPane;

    @FXML
    private HBox centralPane;

    @FXML
    private HBox systemHBox;

    public LoginPresenter(LoginModel model) {
        super(model);
    }

    @Override
    public void addBinding() {
        leftPane.prefWidthProperty().bind(centralPane.widthProperty().divide(2));
        rightPane.prefWidthProperty().bind(centralPane.widthProperty().divide(2));
    }

    @Override
    public void addComponents() {

        form = Form.of(
                Group.of(
                        Field.ofStringType(model.usernameProperty())
                                .label("Username"),
                        Field.ofPasswordType(model.passwordProperty())
                                .label("Password")
                                .required("This field can’t be empty")
                )
        ).title("Login");

        FormRenderer formRendered = new FormRenderer(form);
        PasswordField passwordField = (PasswordField) ((StackPane) ((SimplePasswordControl) ((GridPane) ((GroupRenderer) formRendered.getChildren().get(0)).getChildren().get(0)).getChildren().get(1)).getChildren().get(1)).getChildren().get(0);

        passwordField.setOnAction(actionEvent -> checkLogin());
        systemForm = Form.of(
                Group.of(
                        Field.ofSingleSelectionType(model.allSystemTypeProperty(), model.systemTypeProperty())
                                .label("")
                )
        ).title("System");
        FormRenderer systemFormGroup = new FormRenderer(systemForm);
        systemFormGroup.prefWidthProperty().set(300);

        mainVBox.getChildren().add(
                formRendered
        );
        systemHBox.getChildren().add(systemFormGroup);
    }

    public void checkLogin() {
        try {
            form.persist();
            UserB userB = model.checkLogin();
            if (userB != null) {
                // Load other window

                if (MenuModel.MenuType.POS.equals(model.getSystemType())){
                    Assignment assignment = LoginModel.checkOpenShift();

                    // Load POS Model if it was selected.
                    POSMainModel mainModel = new POSMainModel(userB, assignment);
                    POSMainPresenter mainPresenter = new POSMainPresenter(mainModel);
                    POSMainView posMainView = new POSMainView("/posmain.fxml", mainPresenter);
                    Utils.goForward(posMainView, mainVBox);
                    posMainView.initialize();
                }else{
                    MainModel mainModel = new MainModel(userB);
                    MainPresenter mainPresenter = new MainPresenter(mainModel);
                    Utils.goForward(new LoginView("/main.fxml", mainPresenter), mainVBox);
                }
            } else {
                DialogBuilder.createError("Error", "Invalid username or password", "Invalid username or password").showAndWait();
            }
        } catch (Exception e) {
            DialogBuilder.createExceptionDialog("Error", "Error checking the user in database", e.getMessage(), e).showAndWait();
        }
    }

    @FXML
    void signIn(ActionEvent event) {
        checkLogin();
    }

    @Override
    public void initializeComponents() {

    }

    @Override
    public void entryActions() {

    }
}