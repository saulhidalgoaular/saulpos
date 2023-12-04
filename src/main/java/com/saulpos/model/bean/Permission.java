/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
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
package com.saulpos.model.bean;

import com.saulpos.model.dao.BeanImplementation;
import jakarta.persistence.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

@Entity
@Access(AccessType.PROPERTY)
@Table
//tipo_de_usuario_puede
public class Permission extends BeanImplementation<Permission> {

    private final SimpleObjectProperty<Profile> profile = new SimpleObjectProperty();
    private final ObjectProperty<MenuModel> node = new SimpleObjectProperty<>();

    private final SimpleBooleanProperty granted = new SimpleBooleanProperty();

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
