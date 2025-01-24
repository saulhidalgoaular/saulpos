package com.saulpos.model.dao;

import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.model.LoginModel;
import com.saulpos.model.bean.*;
import com.saulpos.model.menu.DefaultMenuGenerator;
import org.junit.jupiter.api.Test;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.HashSet;

public class DefaultValuesTest {

    @Test
    public void mainTest() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {

        DatabaseConnection.getInstance().initialize();

        HibernateDataProvider hibernateDataProvider = new HibernateDataProvider();
        for (Object object : hibernateDataProvider.getAllItems(UserB.class)){
            final AbstractBean bean = (AbstractBean) object;
            bean.delete();
        }

        for (Object object : hibernateDataProvider.getAllItems(Profile.class)){
            final AbstractBean bean = (AbstractBean) object;
            bean.delete();
        }

        for (Object object : hibernateDataProvider.getAllItems(MenuModel.class)){
            final AbstractBean bean = (AbstractBean) object;
            bean.delete();
        }

        for (Object object : hibernateDataProvider.getAllItems(Permission.class)){
            final AbstractBean bean = (AbstractBean) object;
            bean.delete();
        }

        saveDefaultValues();
        System.out.println("Test");
    }

    public void saveDefaultValues() throws PropertyVetoException, IOException, URISyntaxException, ClassNotFoundException {
        // default password
        UserB admin = new UserB();
        admin.setUserName("admin");
        admin.setName("Saul");
        admin.setLastname("Hidalgo");
        admin.setPassword("admin");
        admin.hashPassword();
        admin.setEnabled(true);
        admin.setShouldChangePassword(true);

        // assign the profile
        Profile newProfile = new Profile();
        admin.setProfile(newProfile);
        newProfile.setName("Administrator");
        newProfile.setDescription("Administrator Profile");
        newProfile.setPermissions(new HashSet<>());

        // give permission to all menus
        DefaultMenuGenerator defaultMenuGenerator = new DefaultMenuGenerator();
        ArrayList<MenuModel> menuModels = defaultMenuGenerator.generateMenu();

        for (MenuModel menuModel : menuModels){
            Permission permission = new Permission();
            permission.setGranted(true);
            permission.setProfile(newProfile);
            permission.setNode(menuModel);

            newProfile.getPermissions().add(permission);
        }

        admin.saveOrUpdate();

        DollarRate dollarRate = new DollarRate();
        dollarRate.setEnabled(true);
        dollarRate.setExchangeRatePerDollar(1);
        dollarRate.setLocalCurrencyName("Test Currency");
        dollarRate.saveOrUpdate();

        Product newProduct = new Product();
        newProduct.setArea("123");
        newProduct.setDescription("Test Product");
        newProduct.setBarcode("123");
        newProduct.setExistence(100);
        newProduct.setBlocked(false);
        newProduct.setBrand("Test Brand");
        newProduct.setPrice(100);
        Discount discount = new Discount();
        discount.setProduct(newProduct);
        discount.setStartingDate(LocalDate.now());
        discount.setEndingDate(LocalDate.now());
        discount.setPercentage(25.);
        newProduct.setDiscount(discount);

        Vat vat = new Vat();
        vat.setDescription("Standard");
        vat.setPercentage(12);
        vat.saveOrUpdate();

        newProduct.setVat(vat);
        newProduct.saveOrUpdate();


        Shift shift = new Shift();
        shift.setShiftName("Standard");
        shift.setShiftStart(LocalTime.MIN);
        shift.setShiftEnd(LocalTime.MAX.minusSeconds(1));
        shift.saveOrUpdate();

        Cashier cashier = new Cashier();
        cashier.setDescription("Caja 1");
        cashier.setEnabled(true);
        cashier.setPrinter("Test Printer");
        cashier.setPhysicalName("024a6ce95c27");
        cashier.saveOrUpdate();

        Assignment assignment = new Assignment();
        assignment.setAssignmentStatus(Assignment.AssignmentStatus.Open);
        assignment.setAssignmentDay(LocalDate.now());
        assignment.setCashier(cashier);
        assignment.setShift(shift);
        assignment.saveOrUpdate();


    }
}
