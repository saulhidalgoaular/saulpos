package com.saulpos.model.bean;

import com.saulpos.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
//tipo_de_usuario_puede
public class TypeOfUserCan extends AbstractBeanImplementation<TypeOfUserCan> {
    //id_tipo_usuario
    @Max(20)
    private SimpleStringProperty userTypeId = new SimpleStringProperty();

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
    public String getUserTypeId(){
        return userTypeId.get();
    }

    public SimpleStringProperty userTypeIdProperty(){
        return userTypeId;
    }

    public void setUserTypeId(String userTypeId){
        this.userTypeId.set(userTypeId);
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
    public void receiveChanges(TypeOfUserCan typeOfUserCan) {

    }

    @Override
    public TypeOfUserCan clone() {
        //Todo
        return null;
    }
}
