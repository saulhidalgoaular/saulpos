package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.model.LoginModel;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.presenter.LoginPresenter;
import com.saulpos.view.LoginView;
import com.saulpos.view.ParentPane;
import com.saulpos.view.Utils;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;

public class LogoutMenuAction implements MenuAction {
    @Override
    public Object run(MainModel mainModel, Pane mainPane) throws Exception {
        //System.exit(0);
        //Utils.goForward(new Utils.ViewDef("/login.fxml"), );

        LoginModel loginModel = new LoginModel();
        LoginPresenter loginPresenter = new LoginPresenter(loginModel, new LoginView());

        ((VBox)(mainPane.getParent())).getChildren().remove(0);

        Utils.goForward(new Utils.ViewDef("/login.fxml", loginPresenter), mainPane);

        return null;
    }
}
