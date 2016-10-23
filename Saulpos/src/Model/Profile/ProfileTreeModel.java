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
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Saúl Hidalgo <saulhidalgoaular at gmail.com>
 */
public class ProfileTreeModel implements Cloneable{
    private ObservableList<ProfileTreeModel> childs;
    private SimpleStringProperty id;
    private SimpleBooleanProperty visible;
    private final String function;
    private final String caption;
    private String profile;

    public ProfileTreeModel() {
        childs = FXCollections.observableArrayList();
        id = new SimpleStringProperty();
        visible = new SimpleBooleanProperty();
        function = "";
        caption = "";
        profile = "";
        addVisibleListener();
    }

    public ProfileTreeModel(ObservableList<ProfileTreeModel> childs,
                     SimpleStringProperty id,
                     SimpleBooleanProperty visible,
                     String function,
                     String caption) {
        
        this.childs = childs;
        this.id = id;
        this.visible = visible;
        this.function = function;
        this.caption = caption;
        profile = "";
        addVisibleListener();
    }

    public ProfileTreeModel(String id, String function, String caption) {
        this.id = new SimpleStringProperty(id);
        this.function = function;
        childs = FXCollections.observableArrayList();
        visible = new SimpleBooleanProperty(true);
        this.caption = caption;
        addVisibleListener();
    }
    
    protected ProfileTreeModel(ProfileTreeModel p){
        // Deep inside 
        
        if ( p.getChilds() == null ){
            childs = null;
        }else{
            childs = FXCollections.observableArrayList();

            for (ProfileTreeModel child : p.getChilds()) {
                childs.add((ProfileTreeModel)child.clone());
            }
        }
        
        id = new SimpleStringProperty(p.getId().getValue());
        visible = new SimpleBooleanProperty(p.getVisible().getValue());
        function = p.getFunction();
        caption = p.getCaption();
        profile = p.getProfile();
        addVisibleListener();
    }
    
    @Override
    public Object clone(){
        return new ProfileTreeModel(this);
    }

    public ObservableList<ProfileTreeModel> getChilds() {
        return childs;
    }

    public SimpleStringProperty getId() {
        return id;
    }

    public String getProfile() {
        return profile;
    }

    public String getCaption() {
        return caption;
    }

    public SimpleBooleanProperty getVisible() {
        return visible;
    }

    public String getFunction() {
        return function;
    }
    
    private void addVisibleListener(){
        visible.addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> ov, Boolean oldValue, Boolean newValue) {
                try {
                    setVisible(newValue);
                    save();
                } catch (SQLException ex) {
                    Logger.getLogger(ProfileTreeModel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
    }
    
    public void save() throws SQLException{
        //TODO: VERIFY CONCURRENCY =(
        Connection c = MainController.getInstance().getConnectionModel().getConnection();
        PreparedStatement stmt = null;
        
        if ( getVisible().get() ){
            // TODO: FIX THIS IGNORE
            stmt = c.prepareCall("insert ignore into profile_has_node( profile_id , node_id ) values ( ? , ? ) ");
        }else{
            stmt = c.prepareCall("delete from profile_has_node where profile_id = ? and node_id = ? ");
        }
        stmt.setString(1, getProfile());
        stmt.setString(2, getId().getValue());
        stmt.executeUpdate();
        
        c.close();
    }
    
    public void setProfileToTree(String profileId) throws SQLException{
        Connection c = MainController.getInstance().getConnectionModel().getConnection();
        
        PreparedStatement stmt = c.prepareCall( "select node_id from profile_has_node where profile_id = ? ");
        stmt.setString(1, profileId);
        ResultSet rs = stmt.executeQuery();
        
        TreeSet<String> nodes = new TreeSet<>();
        while( rs.next() ){
            nodes.add(rs.getString("node_id"));
        }
        
        setVisibleThisNodes(this, nodes, profileId);
        
        rs.close();
        c.close();
    }
    
    private void setVisibleThisNodes(ProfileTreeModel p, TreeSet<String> nodes, String profile){
        p.setProfile(profile);
        if ( nodes.contains( p.getId().getValue()) ){
            p.setVisible(true);
        }
        if ( p.getChilds() != null ){
            for (ProfileTreeModel profileTreeModel : p.getChilds()) {
                setVisibleThisNodes(profileTreeModel, nodes, profile);
            }
        }
    }

    public void setChilds(ObservableList<ProfileTreeModel> childs) {
        this.childs = childs;
    }

    public void setVisible(boolean visible) {
        this.visible.setValue(visible);
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
    
}
