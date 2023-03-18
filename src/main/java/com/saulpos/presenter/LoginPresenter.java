package com.saulpos.presenter;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.saulpos.Main;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.LoginModel;
import com.saulpos.model.bean.UserB;
import com.saulpos.view.LoginView;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class LoginPresenter extends AbstractPresenter<LoginModel, LoginView> {
    @FXML
    public VBox mainVBox;

    private Form form;

    public LoginPresenter(LoginModel nModel) {
        super(nModel);
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

        mainVBox.getChildren().add(
                new FormRenderer(form)
        );

        Button loginBTN = new Button("LOG IN");
        loginBTN.setOnAction(event -> checkLogin());
        mainVBox.getChildren().add(loginBTN);
    }

    public void checkLogin() {
        try {
            form.persist();
            UserB userB = model.checkLogin();
            if (userB != null) {
                // Load other window

                System.out.println("OK");
            } else {
                DialogBuilder.createError("Error", "Invalid username or password", "Please try again.");
            }
        } catch (Exception e) {
            DialogBuilder.createExceptionDialog("Error", "Error checking the user in database", e.getMessage(), e);
        }
    }


    @Override
    public void initializeComponents() {

    }

    @Override
    public void entryActions() {

    }
}