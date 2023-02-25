package com.saulpos.view.menu;

import com.saulpos.model.bean.MenuModel;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class MenuBarGenerator {
    public static MenuBar generateMenuNode(MenuModel[] allMenu) {
        // We need to order them before adding them into the menu;
        HashSet<MenuModel> visited = new HashSet<>();
        // Let's order them using dfs

        ArrayList<MenuModel> order = new ArrayList<>();
        for (int i = 0; i < allMenu.length; i++) {
            topologicalOrder(order, visited, allMenu[i]);
        }

        // we have the order to add them;

        MenuBar menuBar = new MenuBar();

        HashMap<MenuModel, MenuItem> allMenuObjects = new HashMap<>();
        for (MenuModel menu : order) {
            MenuItem newMenu;
            if (menu.getAction() != null && !menu.getAction().isBlank()) {
                newMenu = new MenuItem(menu.getName());
                newMenu.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        menu.getMenuAction().run();
                    }
                });
            } else {
                newMenu = new Menu(menu.getName());
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

    private static void topologicalOrder(ArrayList<MenuModel> order, HashSet<MenuModel> visited, MenuModel menu) {
        if (!visited.contains(menu)) {
            // I need to add my parent first.
            visited.add(menu);

            if (menu.getPredecessor() != null) {
                topologicalOrder(order, visited, menu.getPredecessor());
            }

            order.add(menu);
        }
    }
}
