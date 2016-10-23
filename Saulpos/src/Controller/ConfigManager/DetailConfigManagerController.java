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
package Controller.ConfigManager;

import Controller.Util.MainController;
import View.Util.Dialog;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class DetailConfigManagerController implements Initializable{

    @FXML
    public Label configTitle;
    
    @FXML
    public TextArea configText;
    
    @FXML
    public Label serverUrlLabel;
    
    @FXML
    public Label userNameLabel;
    
    @FXML
    public Label passwordLabel;
    
    @FXML
    public Label actualNameLabel;
    
    @FXML
    public Label idLabel;
    
    @FXML
    public Label comLabel;
    
    @FXML
    public Label fiscalPrinterModelLabel;
    
    @FXML
    public TextField serverUrlTextField;
    
    @FXML
    public TextField userNameTextField;
    
    @FXML
    public TextField comTextField;
    
    @FXML
    public TextField idTextField;
    
    @FXML
    public TextField nameTextField;
    
    @FXML
    public PasswordField passwordText;
    
    @FXML
    public Button testDatabaseConnectionButton;
    
    @FXML
    public ChoiceBox<String> comboFiscalPrinterModel;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        
        if ( comboFiscalPrinterModel != null ){
            comboFiscalPrinterModel.setItems(FXCollections.observableArrayList(MainController.getInstance().getConstants().getCompatibleDrivers()));
        }
        
        MainController.getInstance().getLanguage().translateFrame(this);
        System.out.println("Initialize");
    }
    
    /**
     * Test a connection with the parameters in the labels
     * @param event Action Event
     */
    public void testConnectionAction(ActionEvent event){
        try{
            
            if ( serverUrlTextField.getText().isEmpty() ||
                    userNameTextField.getText().isEmpty() ||
                    passwordText.getText().isEmpty() ){
                // TODO: CUSTOM EXCEPTIONS!!
                throw new IllegalArgumentException();
            }
            
            MainController.getInstance().getConnectionModel().testConnection(
                    serverUrlTextField.getText(),
                    userNameTextField.getText(),
                    passwordText.getText());
            Dialog.showInfo(
                    MainController.getInstance().getLanguage().get("ProjectName"),
                    MainController.getInstance().getLanguage().get("successfullConnection"));
        }catch(IllegalArgumentException ex){
            Dialog.showWarning(
                    MainController.getInstance().getLanguage().get("ProjectName"),
                    MainController.getInstance().getLanguage().get("fillFields"));
        }catch(Exception ex){
            Dialog.showError(
                    MainController.getInstance().getLanguage().get("ProjectName"), 
                    MainController.getInstance().getLanguage().get("notsuccessfullConnection") + "\n\n" +
                    MainController.getInstance().getLanguage().get("reason") + ": " + ex.getLocalizedMessage());
        }
    }
    
    // TODO: Test connection for fiscal printer
    
}
