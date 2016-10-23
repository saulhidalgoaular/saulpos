/*
 * Copyright (C) 2012 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package Controller.Util;

import Controller.ErrHandler.ConfigManagerException;
import Model.Util.ConnectionModel;
import View.Util.Dialog;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import org.apache.log4j.BasicConfigurator;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class MainController extends Application {
    
    // Singleton
    // TODO: FIX THIS
    private static MainController instance;
    
    //<editor-fold defaultstate="collapsed" desc="Main Controllers">
    private LoggerController logger = new LoggerController();
    private LanguageController language = new LanguageController();
    private ConfigurationController configurationController = new ConfigurationController();
    private ConstantsController constants = new ConstantsController();
    private ConnectionModel connectionModel = new ConnectionModel();
    //</editor-fold>
        
    @Override
    public void start(Stage primaryStage) {
        instance = this;
        //createHelloWorld(primaryStage);
        
        initialize();
    }
    
    /**
     * Initialize the app
     */
    private void initialize(){
        /**
         * Configure log4j
         * TODO: A better configuration :-/
         */
        BasicConfigurator.configure();
        
        //<editor-fold defaultstate="collapsed" desc="Spanish as default language">
        // TODO: ADD LANGUAGE TO THE CONFIG FILE
        MainController.getInstance().getConfigurationController().set(
                "language", constants.getDefaultLanguage());
        MainController.getInstance().getConfigurationController().set(
                "country", constants.getDefaultCountry());
        //</editor-fold>
        
        //<editor-fold defaultstate="collapsed" desc="Load Language Controller">
        language.initializeLanguage(
                (String)MainController.getInstance().getConfigurationController().get("language"),
                (String)MainController.getInstance().getConfigurationController().get("country"));
        //</editor-fold>
        
        try {
            /**
             * Load Configuration File
             */
            MainController.getInstance().getConfigurationController().loadConfigurationFile();

            initializePool();
            
            MainController.getInstance().getConfigurationController().loadDatabaseConfiguration();
            
            createLoginView();
            
        } catch (SQLException ex) {
            Dialog.showError(MainController.getInstance().getLanguage().get("ProjectName"),
                    MainController.getInstance().getLanguage().get("errConnectingDB") + " " + MainController.getInstance().getLanguage().get("isServerRunningAndConfigOk"));
        } catch (ConfigManagerException ex) {
            MainController.getInstance().getLogger().error( MainController.class , "Couln't load configuration file =( " + ex.getMessage());
            createConfigFile();
        } catch (PropertyVetoException ex){
            Dialog.showError(MainController.getInstance().getLanguage().get("ProjectName"),
                    MainController.getInstance().getLanguage().get("errCreatingPool") + " " + MainController.getInstance().getLanguage().get("isServerRunningAndConfigOk"));
        }
    }
    
    private void initializePool() throws PropertyVetoException{
        MainController.getInstance().getConnectionModel().initializePool(
                (String)MainController.getInstance().getConfigurationController().get("serverUrl"),
                (String)MainController.getInstance().getConfigurationController().get("userName"),
                (String)MainController.getInstance().getConfigurationController().get("password"), 
                MainController.getInstance().getConstants().getMaxCheckoutTime());
    }
    
    private void createConfigFile(){
        //<editor-fold defaultstate="collapsed" desc="Wanna create it?">
        Dialog.buildConfirmation(MainController.getInstance().getLanguage().get("ProjectName"), MainController.getInstance().getLanguage().get("configNotFoundReconfig"))
                .addYesButton(loadConfigManager())
                .addNoButton(new EventHandler() {
                    
                    @Override
                    public void handle(Event event) {
                        // TODO CREATE DEFAULT CONFIG!!!
                        MainController.getInstance().getLogger().error(MainController.class , "User don't want to create config file! :-/");
                        Dialog.showInfo(MainController.getInstance().getLanguage().get("ProjectName"), MainController.getInstance().getLanguage().get("userDontWantCreateConfigFile"));
                    }
                })
                .build()
                .show();
        //</editor-fold>
    }
    
    private void createLoginView(){
        Parent root;
        try {
            root = FXMLLoader.load(getClass().getResource("/View/Login/Login.fxml"));
            Stage s = new Stage();
            s.setTitle(MainController.getInstance().getLanguage().get("configManagerForm"));
            s.setScene(new Scene(root));
            
            //<editor-fold defaultstate="collapsed" desc="Maximize">
            Screen screen = Screen.getPrimary();
            Rectangle2D bounds = screen.getVisualBounds();
            
            s.setX(bounds.getMinX());
            s.setY(bounds.getMinY());
            s.setWidth(bounds.getWidth());
            s.setHeight(bounds.getHeight());
            //</editor-fold>

            s.show();
            
            root = FXMLLoader.load(getClass().getResource("/View/Profile/ProfileManager.fxml"));
            s = new Stage();
            s.setTitle(MainController.getInstance().getLanguage().get("profileManagerTitle"));
            s.setScene(new Scene(root));  
            s.getScene().getStylesheets().add("themes/DefaultTheme.css");
            s.show();
            
            root = FXMLLoader.load(getClass().getResource("/View/User/UserManager.fxml"));
            s = new Stage();
            s.setTitle(MainController.getInstance().getLanguage().get("userManagerTitle"));
            s.setScene(new Scene(root));  
            s.getScene().getStylesheets().add("themes/DefaultTheme.css");
            s.show();
            
            
        } catch (IOException ex) {
            MainController.getInstance().getLogger().error(MainController.class , "Error Loading Login form =( " + ex.getMessage());
        }
    }
    
    /**
     * Load the configuration manager
     * @return Just an EventHandler
     */
    private EventHandler loadConfigManager(){
        return new EventHandler() {

            @Override
            public void handle(Event event) {
                try {
                    //<editor-fold defaultstate="collapsed" desc="Load Config Manager">
                    Parent root = FXMLLoader.load(getClass().getResource("/View/ConfigManager/ConfigManager.fxml"));
                    Stage s = new Stage();
                    s.setTitle(MainController.getInstance().getLanguage().get("configManagerForm"));
                    s.setResizable(false);
                    s.setScene(new Scene(root));
                    
                    s.show();
                    //</editor-fold>
                } catch (Exception ex) {
                    Logger.getLogger(MainController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
    }
    
    /**
     * Just a Hello World
     */
    public void createHelloWorld(Stage primaryStage){
        Button btn = new Button();
        btn.setText("Say 'Hello World'");
        btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Hello World!");
            }
        });
                
        StackPane root = new StackPane();
        root.getChildren().add(btn);
        
        Scene scene = new Scene(root, 300, 250);
        
        primaryStage.setTitle("Hello World!");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters">
    public static MainController getInstance() {
        return instance;
    }
    
    public LoggerController getLogger() {
        return logger;
    }
    
    public LanguageController getLanguage() {
        return language;
    }
    
    public ConfigurationController getConfigurationController() {
        return configurationController;
    }
    
    public ConstantsController getConstants() {
        return constants;
    }
    
    public ConnectionModel getConnectionModel() {
        return connectionModel;
    }
    //</editor-fold>
}
