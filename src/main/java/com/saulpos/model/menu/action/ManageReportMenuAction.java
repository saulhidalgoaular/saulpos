/*
 * Copyright (C) 2012-2023 Saúl Hidalgo <saulhidalgoaular at gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.model.CrudModel;
import com.saulpos.javafxcrudgenerator.model.Function;
import com.saulpos.javafxcrudgenerator.model.dao.AbstractBean;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.view.*;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.Report;
import com.saulpos.model.bean.ReportColumn;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.model.exception.SaulPosException;
import com.saulpos.model.report.DynamicReportsModel;
import com.saulpos.view.AbstractView;
import com.saulpos.view.Utils;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;

public class ManageReportMenuAction extends CrudMenuAction{

    private AbstractView viewDef;

    public ManageReportMenuAction() {
        super(Report.class);
    }

    @Override
    public void run(MainModel mainModel, Pane mainPane) throws Exception {
        CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();
        crudGeneratorParameter.setClazz(this.crudClass);
        HibernateDataProvider dataProvider = new HibernateDataProvider();

        NodeConstructor columnsButtonConstructor = new NodeConstructor() {
            @Override
            public Node generateNode(Object... name) {
                Button customButton = new Button();
                Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.COLUMNS, crudGeneratorParameter.translate("columns"), "20px", "10px", ContentDisplay.LEFT);
                customButton.setGraphic(icon);
                customButton.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                return customButton;
            }
        };

        Function columnsButtonFunction = new Function() {
            @Override
            public Object[] run(Object[] params) throws Exception {
                Report report = (Report)params[0];

                CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();

                NodeConstructor customBackButtonConstructor = new NodeConstructor() {
                    @Override
                    public Node generateNode(Object... name) {
                        Button customButton = new Button();
                        Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.BACKWARD, crudGeneratorParameter.translate("back"), "20px", "10px", ContentDisplay.LEFT);
                        customButton.setGraphic(icon);
                        customButton.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                        return customButton;
                    }
                };

                Function backButtonFunction = new Function() {
                    @Override
                    public Object[] run(Object[] objects) throws Exception {
                        new ManageReportMenuAction().run(mainModel, mainPane);
                        return null;
                    }
                };

                crudGeneratorParameter.addCustomButton(new CustomButton(customBackButtonConstructor, backButtonFunction));

                crudGeneratorParameter.setClazz(ReportColumn.class);
                HibernateDataProvider dataProviderForColumn = new HibernateDataProvider(){

                    @Override
                    public List getAllItems(Class aClass, AbstractBean abstractBean, SearchType type) {
                        ReportColumn dummyReportColumn = new ReportColumn();
                        dummyReportColumn.setReport(report);

                        try {
                            return DatabaseConnection.getInstance().listBySample(ReportColumn.class, dummyReportColumn, SearchType.EQUAL);
                        } catch (Exception e) {
                            DialogBuilder.createExceptionDialog("Error", "Error query the database", e.getMessage(), e).showAndWait();
                        }

                        return new ArrayList();
                    }
                };

                crudGeneratorParameter.setDataProvider(dataProviderForColumn);
                // FIXME later this is not too elegant, but let's leave it for now...
                CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter){
                    @Override
                    public CrudPresenter generate() throws Exception {
                        final CrudModel model = new CrudModel<>(crudGeneratorParameter){
                            @Override
                            public AbstractBean getNewBean() {
                                ReportColumn newBean = new ReportColumn();
                                newBean.setReport(report);
                                return newBean;
                            }
                        };
                        final CrudView view = new CrudViewGenerator(crudGeneratorParameter).generate();

                        return new CrudPresenter (model, view);
                    }
                };
                CrudPresenter crud = crudGenerator.generate();
                AbstractView view = new AbstractView(crud.getView().getMainView());
                Utils.goForward(view, mainPane);

                return null;
            }
        };

        NodeConstructor runReportButtonConstructor = new NodeConstructor() {
            @Override
            public Node generateNode(Object... name) {
                Button customButton = new Button();
                Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.PLAY, crudGeneratorParameter.translate("run"), "20px", "10px", ContentDisplay.LEFT);
                customButton.setGraphic(icon);
                customButton.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                return customButton;
            }
        };

        Function runReportFunction = new Function() {
            @Override
            public Object[] run(Object[] params) throws Exception {

                Report report = (Report)params[0];
                if (report.getColumns().size() < 2){
                    throw new SaulPosException("A report should have at least 2 columns");
                }

                DynamicReportsModel dynamicReportsModel = new DynamicReportsModel(report, new HibernateDataProvider());

                dynamicReportsModel.run();

                return new Object[0];
            }
        };

        crudGeneratorParameter.addCustomButton(new CustomButton(columnsButtonConstructor, columnsButtonFunction, true));
        crudGeneratorParameter.addCustomButton(new CustomButton(runReportButtonConstructor, runReportFunction, true));

        crudGeneratorParameter.setDataProvider(dataProvider);
        CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        CrudPresenter crud = crudGenerator.generate();
        viewDef = new AbstractView(crud.getView().getMainView());
        Utils.goForward(viewDef, mainPane);
    }
}
