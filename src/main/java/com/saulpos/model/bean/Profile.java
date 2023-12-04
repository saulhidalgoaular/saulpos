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

import com.saulpos.javafxcrudgenerator.annotations.Ignore;
import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
import com.saulpos.model.menu.DefaultMenuGenerator;
import jakarta.persistence.*;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.*;

@Entity
@Access(AccessType.PROPERTY)
@Table
public class Profile  extends BeanImplementation<Profile> {

    private SimpleStringProperty name = new SimpleStringProperty();

    @TableViewColumn(minWidth = 350, prefWidth = 550)
    private SimpleStringProperty description = new SimpleStringProperty();

    @Ignore
    private ObjectProperty<Set<Permission>> permissions = new SimpleObjectProperty<>(new HashSet<>());

    public Profile() {
    }

    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getDescription() {
        return description.get();
    }

    public SimpleStringProperty descriptionProperty() {
        return description;
    }

    public void setDescription(String description) {
        this.description.set(description);
    }

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "profile")
    public Set<Permission> getPermissions() {
        return permissions.get();
    }

    public ObjectProperty<Set<Permission>> permissionsProperty() {
        return permissions;
    }

    public void setPermissions(Set<Permission> permissions) {
        this.permissions.set(permissions);
    }

    @Override
    public String toString() {
        return description.getValue();
    }

    @Transient
    public List<Permission> getSortedPermissions(){
        if (getPermissions() == null){
            return null;
        }

        ArrayList<Permission> orderedPermissions = new ArrayList<>();

        // We need to order them before adding them into the menu;
        HashSet<Permission> visited = new HashSet<>();
        // Let's order them using dfs

        HashMap<MenuModel, Permission> permissionMap = new HashMap<>();
        for (Permission permission : getPermissions()){
            permissionMap.put(permission.getNode(), permission);
        }

        for (Permission permission : getPermissions()) {
            topologicalOrder(orderedPermissions, visited, permission, permissionMap);
        }

        return orderedPermissions;
    }

    public static void topologicalOrder(ArrayList<Permission> order, HashSet<Permission> visited, Permission permission,
                                        HashMap<MenuModel, Permission> permissionMap) {
        if (!visited.contains(permission)) {
            // I need to add my parent first.
            visited.add(permission);

            MenuModel menu = permission.getNode();
            if (menu.getPredecessor() != null) {
                topologicalOrder(order, visited, permissionMap.get(menu.getPredecessor()), permissionMap);
            }

            order.add(permission);
        }
    }

    public void fillMissingPermissions() {
        HashSet<String> currentPermissions = new HashSet<>();

        for (Permission permission : getSortedPermissions()){
            MenuModel currentMenu = permission.getNode();
            currentPermissions.add(currentMenu.getName()); // Let's assume all names are unique
        }

        DefaultMenuGenerator defaultMenuGenerator = new DefaultMenuGenerator();
        ArrayList<MenuModel> menuModels = defaultMenuGenerator.generateMenu();

        for (MenuModel menuModel : menuModels){
            if (!currentPermissions.contains(menuModel.getName())) {
                Permission permission = new Permission();
                permission.setGranted(false);
                permission.setProfile(this);
                permission.setNode(menuModel);

                getPermissions().add(permission);
            }
        }
    }

}
