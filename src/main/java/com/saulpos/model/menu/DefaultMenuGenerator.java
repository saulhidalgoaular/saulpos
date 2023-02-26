package com.saulpos.model.menu;

import com.saulpos.model.bean.MenuModel;

import java.util.ArrayList;

public class DefaultMenuGenerator {
    public ArrayList<MenuModel> generateMenu() {
        ArrayList<MenuModel> answer = new ArrayList<>();

        MenuModel session = new MenuModel("Session", null, null, null, MenuModel.MenuType.Administrative);
        MenuModel system = new MenuModel("System", null, null, null, MenuModel.MenuType.Administrative);
        MenuModel cashierMachine = new MenuModel("Cashier Machines", system, "null", "action", MenuModel.MenuType.Administrative);
        MenuModel shop = new MenuModel("Shop", null, null, null, MenuModel.MenuType.Administrative);

        answer.add(session);
        answer.add(system);
        answer.add(cashierMachine);
        answer.add(shop);

        return answer;
    }
}
