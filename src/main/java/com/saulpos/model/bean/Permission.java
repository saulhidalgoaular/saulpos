package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
//tipo_de_usuario_puede
public class Permission extends BeanImplementation<Permission> {

    private SimpleIntegerProperty id = new SimpleIntegerProperty();

    //id_tipo_usuario
    private SimpleObjectProperty<Profile> profile = new SimpleObjectProperty();

    //id_nodo
    @OneToOne
    @JoinColumn(name = "nodeId")
    /*
     *  Todo: fix this
     *  the error:'One To One' attribute type should not be 'ObjectProperty'
     * */
    private ObjectProperty<MenuModel> node = new SimpleObjectProperty<>();

    @Id
    @GeneratedValue
    public int getId() {
        return id.get();
    }

    public SimpleIntegerProperty idProperty() {
        return id;
    }

    public void setId(int id) {
        this.id.set(id);
    }

    @ManyToOne
    public Profile getProfile(){
        return profile.get();
    }

    public SimpleObjectProperty<Profile> profileProperty() {
        return profile;
    }

    public void setProfile(Profile profile){
        this.profile.set(profile);
    }

    @OneToOne
    public MenuModel getNode() {
        return node.get();
    }

    public void setNode(MenuModel node) {
        this.node.set(node);
    }

    public ObjectProperty<MenuModel> nodeProperty() {
        return node;
    }

    @Override
    public void receiveChanges(Permission typeOfUserCan) {

    }

    @Override
    public Permission clone() {
        //Todo
        return null;
    }
}
