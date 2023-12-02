package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.*;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.Utils;
import javafx.scene.layout.Pane;

import java.util.ArrayList;

public class CrudMenuAction implements MenuAction {

    Class crudClass;


    public CrudMenuAction(Class crudClass) {
        this.crudClass = crudClass;
    }

    @Override
    public Object run(MainModel mainModel, Pane mainPane) throws Exception {
        CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();
        crudGeneratorParameter.setClazz(crudClass);
        HibernateDataProvider dataProvider = new HibernateDataProvider();
        dataProvider.registerClass(Profile.class);
        dataProvider.registerClass(Shift.class);
        dataProvider.registerClass(Cashier.class);
        crudGeneratorParameter.setDataProvider(dataProvider);
        CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        CrudPresenter crud = crudGenerator.generate();
        Utils.goForward(new Utils.ViewDef(crud.getView().getMainView()), mainPane);
        return null;
    }
}
