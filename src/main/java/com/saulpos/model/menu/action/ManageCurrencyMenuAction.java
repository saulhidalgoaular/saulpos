package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.view.DialogBuilder;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.DollarRate;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.AbstractView;
import com.saulpos.view.Utils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

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
                if(currentDollarRate.isEnabled()){
                    disableOtherCurrency();
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
                if(dollarRate.isEnabled()){
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

    private void disableOtherCurrency() throws Exception {
        //Find & deactivated other currency & also update in DB
        EntityManagerFactory entityManagerFactory = DatabaseConnection.getInstance().entityManagerFactory;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<DollarRate> criteriaQuery = criteriaBuilder.createQuery(DollarRate.class);
        Root<DollarRate> root = criteriaQuery.from(DollarRate.class);
        criteriaQuery.select(root).where(criteriaBuilder.equal(root.get("enabled"), true));
        List<DollarRate> resultList = entityManager.createQuery(criteriaQuery).getResultList();
        for (DollarRate dollarRate : resultList) {
            dollarRate.setEnabled(false);
            dollarRate.saveOrUpdate();
        }
        entityManager.close();
    }
}
