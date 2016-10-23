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
import java.util.TreeMap;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ProfileModel {

    private SimpleStringProperty id;
    private SimpleStringProperty description;
    private ProfileTreeModel profileTree;
    
    private static ProfileTreeModel nodesStructure;

    public ProfileModel() {
        id = new SimpleStringProperty();
        description = new SimpleStringProperty();
        profileTree = new ProfileTreeModel();
    }

    public ProfileModel(String name, String description, ProfileTreeModel profileTreeModel) {
        this.id = new SimpleStringProperty(name);
        this.description = new SimpleStringProperty(description);
        this.profileTree = profileTreeModel;
    }

    public ProfileTreeModel getProfileTree() {
        return profileTree;
    }

    public SimpleStringProperty getId() {
        return id;
    }

    public SimpleStringProperty getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = new SimpleStringProperty(description);
    }

    public void setId(String name) {
        this.id = new SimpleStringProperty(name);
    }
    
    public void save(String oldId) throws SQLException{
        //TODO: VERIFY CONCURRENCY =(
        Connection c = MainController.getInstance().getConnectionModel().getConnection();
        PreparedStatement stmt = c.prepareCall("update profile set description = ?, id = ? where id = ? ");
        stmt.setString(1, description.getValue());
        stmt.setString(2, id.getValue());
        stmt.setString(3, oldId);
        stmt.executeUpdate();
        
        c.close();
    }
    
    private static void loadNodesStructure() throws SQLException{
        Connection c = MainController.getInstance().getConnectionModel().getConnection();
        PreparedStatement stmt = c.prepareCall(
                "select id , caption , function, predecessor from node where administrative = 1");
        ResultSet rs = stmt.executeQuery();
        
        TreeMap<String, ObservableList<ProfileTreeModel> > mapNodes = new TreeMap<>();
        while( rs.next() ){
            if ( !mapNodes.containsKey(rs.getString("predecessor"))){
                ObservableList<ProfileTreeModel> list = FXCollections.observableArrayList();
                mapNodes.put(rs.getString("predecessor"), list );
            }
            mapNodes.get(rs.getString("predecessor")).
                    add( new ProfileTreeModel(
                            null,
                            new SimpleStringProperty(rs.getString("id")),
                            new SimpleBooleanProperty(false),
                            rs.getString("function"),
                            rs.getString("caption")));
        }
        
        nodesStructure = new ProfileTreeModel("root","menu","root");
        createTree(nodesStructure, mapNodes);
        
        rs.close();
        c.close();
    }

    public static ProfileTreeModel getNodesStructure() throws SQLException {
        if ( nodesStructure == null ){
            loadNodesStructure();
        }
        return nodesStructure;
    }
    
    private static void createTree(ProfileTreeModel nodes, TreeMap<String, ObservableList<ProfileTreeModel>> mapNodes) {
        nodes.setChilds(mapNodes.get(nodes.getId().getValue()));
        if ( nodes.getChilds() != null ){
            for (ProfileTreeModel profileTreeModel : nodes.getChilds()) {
                createTree(profileTreeModel, mapNodes);
            }
        }
    }
}
