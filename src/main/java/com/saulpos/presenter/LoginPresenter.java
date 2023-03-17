package com.saulpos.presenter;

import com.dlsc.formsfx.model.event.FieldEvent;
import com.dlsc.formsfx.model.structure.*;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.LoginModel;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.LoginView;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.VBox;

public class LoginPresenter extends AbstractPresenter<LoginModel, LoginView> {
    @FXML
    public VBox mainVBox;

    public LoginPresenter(LoginModel nModel) {
        super(nModel);
    }

    @Override
    public void addBinding() {

    }
    @Override
    public void addComponents() {

        Field usernameField = Field.ofStringType(model.getUsername()).label("Username:").id("username-input");
        Field passwordField = Field.ofPasswordType(model.getPassword()).label("Password:").required("This field can’t be empty").id("password-input");

        Form form = Form.of(
                Group.of(usernameField, passwordField)
        ).title("Login");

        mainVBox.getChildren().add(
                new FormRenderer(form)
        );

        Button loginBTN = new Button("LOG IN");
        loginBTN.setOnAction(event -> checkLogin());
        mainVBox.getChildren().add(loginBTN);
    }

    public void checkLogin(){
        if (model.checkLogin()){
            // Load other window
            System.out.println("OK");
        }else{
            DialogBuilder.createError("Error" ,"Invalid username or password", "Please try again.");
        }
    }

    @Override
    public void initializeComponents() {

    }

    @Override
    public void entryActions() {

    }
}