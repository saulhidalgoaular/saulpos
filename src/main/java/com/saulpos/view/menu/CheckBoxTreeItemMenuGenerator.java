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
package com.saulpos.view.menu;

import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.Permission;
import com.saulpos.view.POSIcons;
import javafx.beans.binding.Bindings;
import javafx.scene.control.*;
import org.controlsfx.control.CheckTreeView;

import java.util.Collection;
import java.util.HashMap;

public class CheckBoxTreeItemMenuGenerator {

    public static TreeView<MenuModel> generateMenuNode(Collection<Permission> permissions){
        final CheckTreeView<MenuModel> treeView = new CheckTreeView<>();

        final CheckBoxTreeItem<MenuModel> root = new CheckBoxTreeItem<>(null);
        root.setExpanded(true);
        treeView.setRoot(root);

        HashMap<MenuModel, CheckBoxTreeItem<MenuModel>> allMenuObjects = new HashMap<>();
        for (Permission permission : permissions) {
            MenuModel menu = permission.getNode();

            CheckBoxTreeItem<MenuModel> treeItem = new CheckBoxTreeItem<>(menu);
            Bindings.bindBidirectional(treeItem.selectedProperty(), permission.grantedProperty());
            treeItem.setExpanded(true);

            if (menu.getIcon() != null && !menu.getIcon().isBlank()) {
                treeItem.setGraphic(POSIcons.getGraphic(menu.getIcon()));
            }
            allMenuObjects.put(menu, treeItem);

            if (menu.getPredecessor() == null) {
                // It is a top menu
                root.getChildren().add(treeItem);
            } else {
                // we add it to their parent
                allMenuObjects.get(menu.getPredecessor()).getChildren().add(treeItem);
            }
        }

        return treeView;
    }
}
