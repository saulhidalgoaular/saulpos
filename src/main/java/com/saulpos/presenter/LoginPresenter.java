package com.saulpos.presenter;

import com.dlsc.formsfx.model.structure.Field;
import com.dlsc.formsfx.model.structure.Form;
import com.dlsc.formsfx.model.structure.Group;
import com.dlsc.formsfx.view.renderer.FormRenderer;
import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.LoginModel;
import com.saulpos.model.bean.UserB;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.LoginView;
import javafx.fxml.FXML;
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

        Form form = Form.of(
                Group.of(
                        Field.ofStringType(model.getUsername())
                                .label("Username:"),
                        Field.ofPasswordType(model.getPassword())
                                .label("Password:")
                                .required("This field can’t be empty")
                )

        ).title("Login");


        mainVBox.getChildren().add(
                new FormRenderer(form)
        );

        CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();
        crudGeneratorParameter.setClazz(UserB.class);
        crudGeneratorParameter.setDataProvider(new HibernateDataProvider());
        CrudGenerator<UserB> crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        try {
            CrudPresenter crud = crudGenerator.generate();
            mainVBox.getChildren().add(crud.getView().getMainView());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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