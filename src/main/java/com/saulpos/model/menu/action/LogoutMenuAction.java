package com.saulpos.model.menu.action;

import com.saulpos.model.LoginModel;
import com.saulpos.presenter.LoginPresenter;
import com.saulpos.view.LoginView;
import com.saulpos.view.ParentPane;
import com.saulpos.view.Utils;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LogoutMenuAction implements MenuAction {
    @Override
    public Object run(Pane mainPane) throws Exception {
        //System.exit(0);
        //Utils.goForward(new Utils.ViewDef("/login.fxml"), );

        LoginModel loginModel = new LoginModel();
        LoginPresenter loginPresenter = new LoginPresenter(loginModel, new LoginView());

        ((VBox)(mainPane.getParent())).getChildren().remove(0);

        Utils.goForward(new Utils.ViewDef("/login.fxml", loginPresenter), mainPane);

        return null;
    }
}
