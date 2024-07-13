package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBeanImplementationSoftDelete;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.DollarRate;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.AbstractView;
import com.saulpos.view.Utils;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

import java.time.LocalDateTime;
import java.util.List;

public class ManageCurrencyMenuAction extends CrudMenuAction{

    private AbstractView viewDef;
    private CrudPresenter<DollarRate> crudPresenter;
    private CrudGeneratorParameter<DollarRate> crudGeneratorParameter;
    public ManageCurrencyMenuAction() {
        super(DollarRate.class);
    }

    @Override
    public void run(MainModel mainModel, Pane mainPane) throws Exception {
        crudGeneratorParameter = new CrudGeneratorParameter<>();
        crudGeneratorParameter.setClazz(this.crudClass);
        HibernateDataProvider dataProvider = new HibernateDataProvider();
        crudGeneratorParameter.setDataProvider(dataProvider);

        CrudGenerator<DollarRate> crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        crudPresenter = crudGenerator.generate();

        ((Button)crudPresenter.getView().getSaveButton()).setOnAction(actionEvent -> {
            try{
                DollarRate currentDollarRate = crudPresenter.getModel().getBeanInEdition();
                if(currentDollarRate.isActivated()){
                    deactivateOtherCurrency();
                }
                currentDollarRate.saveOrUpdate();
                crudPresenter.getModel().refreshAction();
                crudPresenter.getView().getTableView().getSelectionModel().selectLast();
            }catch (Exception e){
                DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
            }
        });

        ((Button)crudPresenter.getView().getDeleteButton()).setOnAction(actionEvent -> {
            try{
                DollarRate dollarRate = (DollarRate) crudPresenter.getView().getTableView().getSelectionModel().getSelectedItem();
                if(dollarRate.isActivated()){
                    DialogBuilder.createError("Error", "SAUL POS",
                            "This currency is already being used. Please deactivate it and try again!!!").showAndWait();
                }else{
                    dollarRate.delete();
                    crudPresenter.getModel().refreshAction();
                    crudPresenter.getView().getTableView().getSelectionModel().selectLast();
                }
            } catch (Exception e){
                DialogBuilder.createExceptionDialog("Exception", "SAUL POS", e.getMessage(), e).showAndWait();
            }
        });

        viewDef = new AbstractView(crudPresenter.getView().getMainView());
        Utils.goForward(viewDef, mainPane);
    }

    private void deactivateOtherCurrency() throws Exception {
        //Find & deactivated other currency & also update in DB
        String query = "SELECT * FROM dollarrate WHERE activated=1";
        List<Object[]> allItems = crudGeneratorParameter.getDataProvider().getItems(query);
        if(allItems.size() > 0){
            DollarRate entity = new DollarRate();
            for(Object[] objArr: allItems){
                entity.setId(Integer.parseInt(objArr[0].toString()));
                entity.setBeanStatus(AbstractBeanImplementationSoftDelete.BeanStatus.valueOf(objArr[1].toString()));
                entity.setCreationTime(LocalDateTime.parse(objArr[2].toString().replace(" ", "T")));
                if(objArr[3] != null){
                    entity.setLastModificationTime(LocalDateTime.parse(objArr[3].toString().replace(" ", "T")));
                }
                if(objArr[4] != null){
                    entity.setLocalCurrencyName(objArr[4].toString());
                }
                if(objArr[5] != null){
                    entity.setLocalCurrencyRate(Double.parseDouble(objArr[5].toString()));
                }
                entity.setActivated(false);
                entity.saveOrUpdate();
            }
        }
    }
}
