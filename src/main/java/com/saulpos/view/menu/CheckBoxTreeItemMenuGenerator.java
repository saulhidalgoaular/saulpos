package com.saulpos.view.menu;

import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.view.POSIcons;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.util.Callback;
import org.controlsfx.control.CheckTreeView;

import java.util.ArrayList;
import java.util.HashMap;

public class CheckBoxTreeItemMenuGenerator {

    public static TreeView<MenuModel> generateMenuNode(ArrayList<MenuModel> allMenu){
        final CheckTreeView<MenuModel> treeView = new CheckTreeView<>();

        final CheckBoxTreeItem<MenuModel> root = new CheckBoxTreeItem<>(null);
        root.setExpanded(true);
        treeView.setRoot(root);

        HashMap<MenuModel, CheckBoxTreeItem<MenuModel>> allMenuObjects = new HashMap<>();
        for (MenuModel menu : allMenu) {

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
