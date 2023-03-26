package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.model.bean.Cashier;
import com.saulpos.model.bean.Profile;
import com.saulpos.model.bean.Shift;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.Utils;
import javafx.scene.layout.Pane;

public class CrudMenuAction implements MenuAction {

    private Class crudClass;


    public CrudMenuAction(Class crudClass) {
        this.crudClass = crudClass;
    }

    @Override
    public Object run(Pane mainPane) throws Exception {
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
