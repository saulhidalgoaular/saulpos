package com.saulpos.presenter;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.view.controls.SimplePasswordControl;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.dlsc.formsfx.view.renderer.GroupRenderer;
import com.saulpos.Main;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.LoginModel;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.menu.DefaultMenuGenerator;
import com.saulpos.view.LoginView;
import com.saulpos.view.Utils;
import com.saulpos.view.menu.MenuBarGenerator;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.HashSet;

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
                MainPresenter mainPresenter = new MainPresenter(mainModel);
                Utils.goForward(new Utils.ViewDef("/main.fxml", mainPresenter), mainVBox);
            } else {
                DialogBuilder.createError("Error", "Invalid username or password", "Please try again.").showAndWait();
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