package com.saulpos.view.menu;

import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.view.POSIcons;
import de.jensd.fx.glyphs.GlyphsDude;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MenuBarGenerator {
    public static MenuBar generateMenuNode(ArrayList<MenuModel> allMenu, Pane mainPane) {

        // we have the order to add them;

        MenuBar menuBar = new MenuBar();

        HashMap<MenuModel, MenuItem> allMenuObjects = new HashMap<>();
        for (MenuModel menu : allMenu) {
            MenuItem newMenu;

            if (menu.getAction() != null && !menu.getAction().isBlank()) {
                newMenu = new MenuItem(menu.getName());
                newMenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        try {
                            menu.getMenuAction().run(allMenu, mainPane);
                        }
                        catch (Exception e){
                            DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
                        }
                    }
                });
            } else {
                newMenu = new Menu(menu.getName());
            }
            if (menu.getIcon() != null && !menu.getIcon().isBlank()) {
                newMenu.setGraphic(POSIcons.getGraphic(menu.getIcon()));
            }
            allMenuObjects.put(menu, newMenu);

            if (menu.getPredecessor() == null) {
                // It is a top menu
                menuBar.getMenus().add((Menu) newMenu);
            } else {
                // we add it to their parent
                ((Menu) allMenuObjects.get(menu.getPredecessor())).getItems().add(newMenu);
            }
        }

        return menuBar;
    }
}
