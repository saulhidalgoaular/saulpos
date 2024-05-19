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

import com.saulpos.model.LoginModel;
import com.saulpos.model.MainModel;
import com.saulpos.presenter.LoginPresenter;
import com.saulpos.view.LoginView;
import com.saulpos.view.Utils;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class LogoutMenuAction implements MenuAction {
    @Override
    public void run(MainModel mainModel, Pane mainPane) throws Exception {
        //System.exit(0);
        //Utils.goForward(new Utils.ViewDef("/login.fxml"), );

        LoginModel loginModel = new LoginModel();
        LoginPresenter loginPresenter = new LoginPresenter(loginModel);
        LoginView loginView = new LoginView("/login.fxml", loginPresenter);

        ((VBox)(mainPane.getParent())).getChildren().remove(0);

        Utils.goForward(loginView, mainPane);

    }
}
