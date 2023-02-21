package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementation;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
//tipo_de_usuario_puede
public class TypeOfUserCan extends AbstractBeanImplementation {
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
    private ObjectProperty<Node> node = new SimpleObjectProperty<>();



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

    public Node getNode() {
        return node.get();
    }

    public ObjectProperty<Node> nodeProperty() {
        return node;
    }

    public void setNode(Node node) {
        this.node.set(node);
    }

    @Override
    public void receiveChanges(AbstractBean currentBean) {
        //Todo
    }

    @Override
    public AbstractBeanImplementation clone() {
        //Todo
        return null;
    }
}
