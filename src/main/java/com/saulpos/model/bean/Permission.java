package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
//tipo_de_usuario_puede
public class Permission extends BeanImplementation<Permission> {

    private SimpleObjectProperty<Profile> profile = new SimpleObjectProperty();
    private ObjectProperty<MenuModel> node = new SimpleObjectProperty<>();

    private SimpleBooleanProperty granted = new SimpleBooleanProperty();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "profile_id")
    public Profile getProfile(){
        return profile.get();
    }

    public SimpleObjectProperty<Profile> profileProperty() {
        return profile;
    }

    public void setProfile(Profile profile){
        this.profile.set(profile);
    }

    @OneToOne(cascade = CascadeType.ALL)
    public MenuModel getNode() {
        return node.get();
    }

    public void setNode(MenuModel node) {
        this.node.set(node);
    }

    public ObjectProperty<MenuModel> nodeProperty() {
        return node;
    }

    public boolean isGranted() {
        return granted.get();
    }

    public SimpleBooleanProperty grantedProperty() {
        return granted;
    }

    public void setGranted(boolean granted) {
        this.granted.set(granted);
    }
}
