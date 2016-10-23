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
import Model.Config.ConfigManagerModel;
import View.Util.Dialog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ConfigManagerController implements Initializable{

    @FXML
    public Label setupProgress;
    
    @FXML
    public Label dataBaseConnection;
    
    @FXML
    public Label dataBaseOfflineConnection;
    
    @FXML
    public Label administratorSetting;
    
    @FXML
    public Label fiscalPrinterSetting;
    
    @FXML
    public Label stickerSetting;
    
    @FXML
    public Label posSetting;
    
    @FXML
    public Label setupProgressBar;
    
    @FXML
    public StackPane mainPane;
    
    @FXML
    public ProgressBar progressBar;
    
    @FXML
    public ImageView imageView;
    
    @FXML
    public ImageView checkImg0;
    
    @FXML
    public ImageView checkImg1;
    
    @FXML
    public ImageView checkImg2;
    
    @FXML
    public ImageView checkImg3;
    
    @FXML
    public Button continueButton;
    
    @FXML
    public Button closeButton;
    
    @FXML
    public Button saveButton;
    
    private Image checkImg;
    
    /**
     * Place for the frames.
     */
    private Parent[] mainFrames;
    
    /**
     * Current frame
     */
    private int currentFrame = 0;
    
    // TODO: IS IT REALLY NEEDED?
    private DetailConfigManagerController[] childWindowsControllers;
    
    private ConfigManagerModel model;
    
    public ConfigManagerController() throws IOException, Exception {
        super();
        MainController.getInstance().getLogger().info(ConfigManagerController.class , "Loaded configuration manager!");
    }

    @Override
    public void initialize(URL location_, ResourceBundle resources_) {
        try {
        
            /**
             * Loading the childs
             */
            String[] urls2load = {"/View/ConfigManager/DatabaseConnection.fxml",
                                  "/View/ConfigManager/DatabaseConnectionOffline.fxml",
                                  "/View/ConfigManager/FiscalPrinter.fxml"};
            
            childWindowsControllers = new DetailConfigManagerController[ urls2load.length ];
            mainFrames = new Parent[ urls2load.length ];
            
            for ( int i = 0 ; i <  urls2load.length ; i++ ) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                URL url = getClass().getResource( urls2load[i] );
                fxmlLoader.setLocation(url);
                fxmlLoader.setBuilderFactory(new JavaFXBuilderFactory());
                mainFrames[i] = (Parent)fxmlLoader.load(url.openStream());
                childWindowsControllers[i] = (DetailConfigManagerController) fxmlLoader.getController();
            }
            
            mainPane.getChildren().add( mainFrames[0] );
            
            /**
             * Creating the bindings
             */
            model = new ConfigManagerModel();
            Bindings.bindBidirectional( model.getServerUrl() , childWindowsControllers[0].serverUrlTextField.textProperty() );
            Bindings.bindBidirectional( model.getUserName() , childWindowsControllers[0].userNameTextField.textProperty() );
            Bindings.bindBidirectional( model.getPassword() , childWindowsControllers[0].passwordText.textProperty() );
            
            Bindings.bindBidirectional( model.getServerOfflineUrl() , childWindowsControllers[1].serverUrlTextField.textProperty() );
            Bindings.bindBidirectional( model.getUserNameOffline() , childWindowsControllers[1].userNameTextField.textProperty() );
            Bindings.bindBidirectional( model.getPasswordOffline() , childWindowsControllers[1].passwordText.textProperty() );
            
            Bindings.bindBidirectional( model.getPosId() , childWindowsControllers[2].idTextField.textProperty() );
            Bindings.bindBidirectional( model.getComPort() , childWindowsControllers[2].comTextField.textProperty() ) ;
            model.getFiscalPrinterModel().bind(childWindowsControllers[2].comboFiscalPrinterModel.getSelectionModel().selectedItemProperty());
            
            /**
             * Loading Image
             */
            imageView.setImage(new Image(MainController.getInstance().getConstants().getDefaultImagesDir() + File.separator + "logo_800x100.jpg"));
            checkImg = new Image(MainController.getInstance().getConstants().getDefaultImagesDir() + File.separator + "check.png");
            
            MainController.getInstance().getLanguage().translateFrame(this);
            System.out.println("Initialize");
        } catch (Exception ex) {
            MainController.getInstance().getLogger().error( ConfigManagerController.class, "Error initialize! " + ex.getMessage());
        }
    }
    
    /**
     * closeButton Action with confirmation dialog
     * @param event Action Event
     */
    public void closeButtonAction(ActionEvent event){
        {
        /**
        * Wanna create it?
        */
           Dialog.buildConfirmation(MainController.getInstance().getLanguage().get("ProjectName"),
                   MainController.getInstance().getLanguage().get("exitConfirm"))

               .addYesButton(new EventHandler() {

                    @Override
                    public void handle(Event event) {
                        System.exit(0);
                    }
                })
               .addNoButton(new EventHandler() {

                   @Override
                   public void handle(Event event) {
                       // Don't do anything
                   }
               })
               .build()
               .show();
       }
    }
    
    /**
     * Show the next form.
     * @param event Action Event
     */
    public void continueButtonAction(ActionEvent event){
        // TODO: IMPORTANT, VALIDATE FIELDS BEFORE GO TO THE NEXT FRAME!!!!
        model.setIsPos(true);
        
        switch ( currentFrame ){
            case 0:
                checkImg0.setImage(checkImg);
                break;
            case 1:
                checkImg1.setImage(checkImg);
                break;
            case 2:
                checkImg2.setImage(checkImg);
                break;
            case 3:
                checkImg3.setImage(checkImg);
                break;
        }
        
        mainFrames[currentFrame].setVisible(false);
        currentFrame++;
        mainPane.getChildren().add(mainFrames[currentFrame]);
        
        /** Progress */
        progressBar.setProgress( (double)currentFrame / (double)mainFrames.length );
        
        if ( currentFrame == mainFrames.length - 1 ){
            continueButton.setVisible(false);
        }
        
        if ( currentFrame == 1 ){
            saveButton.setDisable(true);
        }else{
            saveButton.setDisable(false);
        }
    }
    
    public void saveButtonAction(ActionEvent event){
        try {
            model.save();
            Dialog.showInfo(MainController.getInstance().getLanguage().get("ProjectName"), MainController.getInstance().getLanguage().get("saveSuccessfully"));
        } catch (FileNotFoundException ex) {
            MainController.getInstance().getLogger().error( ConfigManagerController.class, "Error saving configFile! " + ex.getMessage());
        } catch (IOException ex) {
            MainController.getInstance().getLogger().error( ConfigManagerController.class, "Error saving configFile! " + ex.getMessage());
        }
    }
}
