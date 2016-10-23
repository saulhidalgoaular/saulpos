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
package Model.Profile;

import Controller.Util.MainController;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import Controller.ErrHandler.SaulPosException;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ProfileManagerModel {
    private ObservableList<ProfileModel> profileList = FXCollections.observableArrayList();
    private SimpleStringProperty nameText = new SimpleStringProperty();
    private SimpleStringProperty idText = new SimpleStringProperty();

    public ProfileManagerModel() {
    }

    public ObservableList<ProfileModel> getProfileList() {
        return profileList;
    }

    public SimpleStringProperty getNameText() {
        return nameText;
    }

    public SimpleStringProperty getIdText() {
        return idText;
    }
    
    public void loadProfileList(String like) throws SQLException{
        
        profileList.clear();
        
        Connection c = MainController.getInstance().getConnectionModel().getConnection();
        PreparedStatement stmt = c.prepareCall("select id , description from profile where id like ? or description like ?");
        stmt.setString(1, "%" + like + "%");
        stmt.setString(2, "%" + like + "%");
        
        ResultSet rs = stmt.executeQuery();
        while( rs.next() ){
            ProfileTreeModel profileTree = (ProfileTreeModel) ProfileModel.getNodesStructure().clone();
            profileTree.setProfileToTree(rs.getString("id"));
            profileList.add(new ProfileModel( rs.getString("id") , rs.getString("description") , profileTree));
        }
        
        rs.close();
        c.close();
    }
    
    public void createProfile() throws SQLException, SaulPosException{
        // TODO: VERIFY CONCURRENCY
        
        for (ProfileModel profileModel : profileList) {
            if ( profileModel.getId().getValue().equals(idText.getValue()) ){
                throw new SaulPosException(MainController.getInstance().getLanguage().get("repeatedKey"));
            }
        }
        
        Connection c = MainController.getInstance().getConnectionModel().getConnection();
        
        PreparedStatement stmt = c.prepareCall("insert into profile (id,description) values (?,?) ");
        stmt.setString(1, idText.getValue());
        stmt.setString(2, nameText.getValue());
        stmt.executeUpdate();
        
        c.close();
        
        ProfileModel p = new ProfileModel(idText.getValue(), nameText.getValue(), null);
        profileList.add(p);
        
        idText.set("");
        nameText.set("");
    }

    public void checkPrimaryKeyProfile( String newValue ) throws SaulPosException {
        for (ProfileModel profileModel : profileList) {
            if ( profileModel.getId().getValue().equals(newValue) ){
                throw new SaulPosException(MainController.getInstance().getLanguage().get("repeatedKey"));
            }
        }
    }
}
