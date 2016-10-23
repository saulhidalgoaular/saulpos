/*
 * Copyright (C) 2012 Saúl Hidalgo <saulhidalgoaular at gmail.com>
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
package Controller.Login;

import Controller.Util.MainController;
import Model.Login.LoginModel;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class LoginController implements Initializable{

    @FXML
    public Label username;
    
    @FXML
    public Label password;
    
    @FXML
    public TextField userNameTextField;
    
    @FXML
    public PasswordField passwordField;
    
    private LoginModel model;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        
        /**
         * Creating binding
         */
        model = new LoginModel();
        Bindings.bindBidirectional(model.getUsername(), userNameTextField.textProperty());
        Bindings.bindBidirectional(model.getPassword(), passwordField.textProperty());
        
        MainController.getInstance().getLanguage().translateFrame(this);
        System.out.println("Initialize");
    }
    
}
