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
import com.saulpos.model.bean.UserB;
import com.saulpos.view.LoginView;
import com.saulpos.view.MainView;
import com.saulpos.view.Utils;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class LoginPresenter extends AbstractPresenter<LoginModel, LoginView> {
    @FXML
    public VBox mainVBox;

    private Form form;

    public LoginPresenter(LoginModel model, LoginView view) {
        super(model, view);
    }

    @Override
    public void addBinding() {

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
        mainVBox.getChildren().add(
                formRendered
        );

    }

    public void checkLogin() {
        try {
            form.persist();
            UserB userB = model.checkLogin();
            if (userB != null) {
                // Load other window
                MainModel mainModel = new MainModel(userB);
                MainPresenter mainPresenter = new MainPresenter(mainModel, new MainView());
                Utils.goForward(new Utils.ViewDef("/main.fxml", mainPresenter), mainVBox);
            } else {
                DialogBuilder.createError("Error", "Invalid username or password", "Invalid username or password").showAndWait();
            }
        } catch (Exception e) {
            DialogBuilder.createExceptionDialog("Error", "Error checking the user in database", e.getMessage(), e).showAndWait();
        }
    }


    @Override
    public void initializeComponents() {

    }

    @Override
    public void entryActions() {

    }
}