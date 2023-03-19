package com.saulpos.model.menu;

import com.saulpos.model.bean.MenuModel;

import java.util.ArrayList;

public class DefaultMenuGenerator {
    public ArrayList<MenuModel> generateMenu() {
        ArrayList<MenuModel> answer = new ArrayList<>();

        // SESSION ROOT MENU

        MenuModel session = new MenuModel("Session", null, null, null, MenuModel.MenuType.Administrative);
        answer.add(session);

        MenuModel killSession = new MenuModel("Kill Session (Exit)", session, "null", "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(killSession);

        // SYSTEM ROOT MENU

        MenuModel system = new MenuModel("System", null, null, null, MenuModel.MenuType.Administrative);
        answer.add(system);

        MenuModel cashierMachine = new MenuModel("Cash Registers", system, null, "ManageCashierMenuAction", MenuModel.MenuType.Administrative);
        answer.add(cashierMachine);

        MenuModel configuration = new MenuModel("Configuration", system, null, "ManageConfigMenuAction", MenuModel.MenuType.Administrative);
        answer.add(configuration);

        MenuModel configureStore = new MenuModel("Configure Store", system, null, "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(configureStore);

        MenuModel profiles = new MenuModel("Profiles", system, null, "ManageProfileMenuAction", MenuModel.MenuType.Administrative);
        answer.add(profiles);

        MenuModel bankPOS = new MenuModel("Bank Point of Sale", system, null, "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(bankPOS);

        MenuModel resendSales = new MenuModel("Resend Sales", system, null, "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(resendSales);

        MenuModel users = new MenuModel("Users", system, null, "ManageUserMenuAction", MenuModel.MenuType.Administrative);
        answer.add(users);

        // SHOP ROOT MENU

        MenuModel shop = new MenuModel("Shop", null, null, null, MenuModel.MenuType.Administrative);
        answer.add(shop);

        MenuModel product = new MenuModel("Products", shop, null, "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(product);

        MenuModel assignment = new MenuModel("Assignments", shop, null, "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(assignment);

        MenuModel closeDay = new MenuModel("Close Day", shop, null, "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(closeDay);

        MenuModel messages = new MenuModel("Messages", shop, null, "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(messages);

        MenuModel configurePrinter = new MenuModel("Configure Printer", shop, null, "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(configurePrinter);

        MenuModel reports = new MenuModel("Reports", shop, null, "LogoutMenuAction", MenuModel.MenuType.Administrative);
        answer.add(reports);

        MenuModel shifts = new MenuModel("Shifts", shop, null, "TestMenuAction", MenuModel.MenuType.Administrative);
        answer.add(shifts);

        return answer;
    }
}