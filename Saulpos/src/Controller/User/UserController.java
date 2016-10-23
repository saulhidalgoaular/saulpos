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
package Controller.User;

import Controller.Util.MainController;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class UserController implements Initializable{

    @FXML
    public Label userNameLabel;
    
    @FXML
    public Label passwordLabel;
    
    @FXML
    public Label passwordLabelAgain;
    
    @FXML
    public Label nameLabel;
    
    @FXML
    public Label profileLabel;
    
    @FXML
    public Label userNameRealLabel;
    
    @FXML
    public PasswordField passwordField;
    
    @FXML
    public PasswordField passwordFieldAgain;
    
    @FXML
    public TextField userNameField;
    
    @FXML
    public ComboBox<String> profileChoiceBox;
    
    @FXML
    public CheckBox useSamePasswordCheckBox;
    
    @FXML
    public CheckBox changePasswordAfterLoginCheckBox;
    
    @FXML
    public CheckBox lockedCheckBox;
    
    @FXML
    public Button exitButton;
    
    @FXML
    public Button saveButton;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        MainController.getInstance().getLanguage().translateFrame(this);
    }
    
    public void addButtonAction(ActionEvent event){
        
    }
    
}
