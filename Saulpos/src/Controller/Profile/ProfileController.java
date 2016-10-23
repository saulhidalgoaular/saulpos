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
package Controller.Profile;

import Controller.ErrHandler.SaulPosException;
import Controller.Util.MainController;
import Model.Profile.ProfileManagerModel;
import Model.Profile.ProfileModel;
import Model.Profile.ProfileTreeModel;
import View.Util.Dialog;
import View.Util.SearchBox;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.util.Callback;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ProfileController implements Initializable{

    @FXML
    public Pane searchPane;
    
    @FXML
    public TableColumn idColumn;
    
    @FXML
    public TableColumn nameColumn;
    
    @FXML
    public AnchorPane treeViewPane;
    
    @FXML
    public TableView profileView;
    
    @FXML
    public TextField idTextField;
    
    @FXML
    public TextField nameTextField;
    
    @FXML
    public Button AddButton;
    
    @FXML
    public TreeView treeView;
    
    @FXML
    public Label profileTreeLabel;
    
    private SearchBox searchBox;
    
    private StringProperty searchText;
    
    private ProfileManagerModel model;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            searchBox = new SearchBox(276,24);
            searchPane.getChildren().add( searchBox );
            
            model = new ProfileManagerModel();
            
            Bindings.bindContentBidirectional(model.getProfileList(), profileView.getItems());
            nameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProfileModel,String>,ObservableValue<String>>(){

                @Override
                public ObservableValue<String> call(CellDataFeatures<ProfileModel, String> p) {
                    return p.getValue().getDescription();
                }
                
            });
            idColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProfileModel,String>,ObservableValue<String>>(){

                @Override
                public ObservableValue<String> call(CellDataFeatures<ProfileModel, String> p) {
                    return p.getValue().getId();
                }
                
            });
            
            searchText = new SimpleStringProperty();
            Bindings.bindBidirectional(searchText, searchBox.getTextProperty());
                        
            searchText.addListener(new ChangeListener<String>(){
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    try {
                        model.loadProfileList(searchText.getValue());
                    } catch (SQLException ex) {
                        Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            Bindings.bindBidirectional(model.getIdText(), idTextField.textProperty());
            Bindings.bindBidirectional(model.getNameText(), nameTextField.textProperty());
            
            profileView.setEditable(true);
            nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            nameColumn.setOnEditCommit(
                    new EventHandler<CellEditEvent<ProfileModel, String>>() {

                @Override
                public void handle(CellEditEvent<ProfileModel, String> t) {
                    try {
                        ProfileModel p = t.getTableView().getItems().get(t.getTablePosition().getRow());
                        p.setDescription(t.getNewValue());
                        p.save(p.getId().getValue());
                    } catch (SQLException ex) {
                        Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            idColumn.setCellFactory(TextFieldTableCell.forTableColumn());
            idColumn.setOnEditCommit(
                    new EventHandler<CellEditEvent<ProfileModel, String>>() {

                @Override
                public void handle(CellEditEvent<ProfileModel, String> t) {
                    ProfileModel p = t.getTableView().getItems().get(t.getTablePosition().getRow());
                    String oldName = p.getId().getValue();
                    try {
                        model.checkPrimaryKeyProfile( t.getNewValue() );
                        p.setId(t.getNewValue());
                        p.save(oldName);
                    } catch (SaulPosException ex) {
                        try {
                            Dialog.showError(MainController.getInstance().getLanguage().get("ProjectName"), ex.getMessage());
                            model.loadProfileList(searchText.getValue());
                        } catch (SQLException ex1) {
                            Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex1);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
                    } 
                }
            });
            
            profileView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {

                @Override
                public void changed(ObservableValue ov, Object t, Object t1) {
                    try {
                        if ( t1 != null ){
                            renderProfileTree((ProfileModel)t1);
                        }
                    } catch (SQLException ex) {
                        Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            });
            
            treeView.setEditable(true);
            treeView.setCellFactory(CheckBoxTreeCell.<String>forTreeView());
            treeView.setShowRoot(true);
            
            model.loadProfileList(searchText.getValue());
            
            
            MainController.getInstance().getLanguage().translateFrame(this);
        } catch (SQLException ex) {
            Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addButtonAction(ActionEvent event){
        try {
            model.createProfile();
        } catch (SQLException ex) {
            Logger.getLogger(ProfileController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SaulPosException ex) {
            Dialog.showError(MainController.getInstance().getLanguage().get("ProjectName"), ex.getMessage());
        }
    }
    
    /**
     * Render a tree with checkboxes from the model
     * @param profileModel Tree to render
     * @throws SQLException 
     */
    private void renderProfileTree(ProfileModel profileModel) throws SQLException {
        
        CheckBoxTreeItem<String> root = new CheckBoxTreeItem<>(
                MainController.getInstance().getLanguage().get("root"));
        
        root.setExpanded(true);
        root.setIndependent(true);
        treeView.setRoot(root);
        
        createProfileItem(root , profileModel.getProfileTree());
        //profileModel.getProfileTree().setProfileToTree(profileModel.getId().getValue());
    }

    /**
     * Helper of the renderProfileTree. It just renderes a node
     * @param root Node in the TreeView
     * @param profileModel Model
     */
    private void createProfileItem(CheckBoxTreeItem<String> root, ProfileTreeModel profileModel) {
        if ( profileModel.getChilds() != null ){
            
            for (ProfileTreeModel profileTreeModel : profileModel.getChilds()) {
                CheckBoxTreeItem<String> treeItem = new CheckBoxTreeItem<>();
                root.getChildren().add(treeItem);
                
                treeItem.setIndependent(true);
                treeItem.setValue(MainController.getInstance().getLanguage().get(profileTreeModel.getCaption()));
                
                // TODO: CHECK THIS
                treeItem.setSelected( profileTreeModel.getVisible().getValue() );
                
                Bindings.bindBidirectional( profileTreeModel.getVisible(), treeItem.selectedProperty());
                
                createProfileItem( treeItem , profileTreeModel );
            }
        }
    }
    
}
