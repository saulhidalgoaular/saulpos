package com.saulpos.view.menu;

import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.Permission;
import com.saulpos.view.POSIcons;
import javafx.scene.control.*;
import org.controlsfx.control.CheckTreeView;

import java.util.HashMap;
import java.util.Set;

public class CheckBoxTreeItemMenuGenerator {

    public static TreeView<MenuModel> generateMenuNode(Set<Permission> permissions){
        final CheckTreeView<MenuModel> treeView = new CheckTreeView<>();

        final CheckBoxTreeItem<MenuModel> root = new CheckBoxTreeItem<>(null);
        root.setExpanded(true);
        treeView.setRoot(root);

        HashMap<MenuModel, CheckBoxTreeItem<MenuModel>> allMenuObjects = new HashMap<>();
        for (Permission permission : permissions) {
            MenuModel menu = permission.getNode();

            CheckBoxTreeItem<MenuModel> treeItem = new CheckBoxTreeItem<>(menu);
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
