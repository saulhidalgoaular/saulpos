package com.saulpos.model.bean;

import com.saulpos.javafxcrudgenerator.annotations.Ignore;
import com.saulpos.javafxcrudgenerator.annotations.TableViewColumn;
import com.saulpos.model.dao.BeanImplementation;
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
    private ObjectProperty<Set<Permission>> permissions = new SimpleObjectProperty<>();

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

    @OneToMany(cascade = CascadeType.ALL)
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
        // First let's build back the relationships.

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
}
