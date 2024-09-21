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
import com.saulpos.model.bean.Discount;
import com.saulpos.model.bean.Product;
import com.saulpos.model.bean.Vat;
import com.saulpos.model.dao.DatabaseConnection;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.AbstractView;
import com.saulpos.view.Utils;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.DefaultPropertyEditorFactory;
import org.controlsfx.property.editor.Editors;
import org.controlsfx.property.editor.PropertyEditor;

import java.util.ArrayList;
import java.util.List;

public class ManageProductsMenuAction extends CrudMenuAction{

    private AbstractView viewDef;

    public ManageProductsMenuAction() {
        super(Product.class);
    }

    @Override
    public void run(MainModel mainModel, Pane mainPane) throws Exception {
        CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();
        crudGeneratorParameter.setClazz(this.crudClass);
        HibernateDataProvider dataProvider = new HibernateDataProvider();

        NodeConstructor customButtonConstructor = new NodeConstructor() {
            @Override
            public Node generateNode(Object... name) {
                Button customButton = new Button();
                Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.DIAMOND, crudGeneratorParameter.translate("Pricing"), "20px", "10px", ContentDisplay.LEFT);
                customButton.setGraphic(icon);
                customButton.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                return customButton;
            }
        };

        NodeConstructor discountButtonConstructor = new NodeConstructor() {
            @Override
            public Node generateNode(Object... name) {
                Button customButton = new Button();
                Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.TAG, crudGeneratorParameter.translate("Discount"), "20px", "10px", ContentDisplay.LEFT);
                customButton.setGraphic(icon);
                customButton.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                return customButton;
            }
        };
        Function discountButtonFunction = new Function() {
            @Override
            public Object[] run(Object[] params) throws Exception {
                Product product = (Product) params[0];
                Label label = new Label("Assign the discount for the product:");
                VBox vBox = new VBox(5);
                vBox.setPadding(new Insets(30));

                CrudGeneratorParameter<Discount> crudDiscountParamGenerator = new CrudGeneratorParameter<>();
                crudDiscountParamGenerator.setClazz(Discount.class);

                HibernateDataProvider discountDataProvider = new HibernateDataProvider(){
                    @Override
                    public List<Discount> getAllItems(Class aClass, AbstractBean abstractBean, SearchType type) {
                        Discount discount = new Discount();
                        discount.setProduct(product);
                        try {
                            List list = DatabaseConnection.getInstance().listBySample(Discount.class, discount, SearchType.EQUAL);
                            return list;
                        } catch (Exception e) {
                            DialogBuilder.createExceptionDialog("Error", "Error query the database", e.getMessage(), e).showAndWait();
                        }
                        return new ArrayList<>();
                    }
                };

                //Back button constructor
                NodeConstructor discountBackButtonConstructor = new NodeConstructor() {
                    @Override
                    public Node generateNode(Object... objects) {
                        Button customBackButton = new Button();
                        Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.BACKWARD, crudDiscountParamGenerator.translate("Back"), "20px", "10px", ContentDisplay.LEFT);
                        customBackButton.setGraphic(icon);
                        customBackButton.setPrefWidth(crudDiscountParamGenerator.getButtonWidth());
                        return customBackButton;
                    }
                };
                Function discountBackButtonFunction = new Function() {
                    @Override
                    public Object[] run(Object[] objects) throws Exception {
                        if(viewDef != null){
                            Utils.goBack(viewDef, mainPane);
                        }
                        return null;
                    }
                };
                crudDiscountParamGenerator.addCustomButton(new CustomButton(discountBackButtonConstructor, discountBackButtonFunction, false));
                crudDiscountParamGenerator.setDataProvider(discountDataProvider);
                CrudGenerator<Discount> crudDiscountGenerator = new CrudGenerator<>(crudDiscountParamGenerator){
                    @Override
                    public CrudPresenter<Discount> generate() throws Exception {
                        final CrudModel<Discount> model = new CrudModel<>(crudDiscountParamGenerator){
                            @Override
                            public Discount getNewBean() {
                                Discount newBean = new Discount();
                                product.setDiscount(newBean);
                                return newBean;
                            }
                        };
                        final CrudView view = new CrudViewGenerator(crudDiscountParamGenerator).generate();
                        return new CrudPresenter<Discount>(model, view);
                    }
                };
                CrudPresenter<Discount> discountCrudPresenter = crudDiscountGenerator.generate();
                ((Button) discountCrudPresenter.getView().getSaveButton()).setOnAction(
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                try{
                                    if(discountCrudPresenter.getView().getTableView().getItems().size() > 0 &&
                                            discountCrudPresenter.getView().getTableView().getSelectionModel().getSelectedItem() ==null){
                                        DialogBuilder.createError("Error", "SAUL POS",
                                                "Adding multiple discount information for a single product is not allowed!").showAndWait();
                                    }else{
                                        //Condition for not to add the empty discount details
                                        if(!discountCrudPresenter.getModel().getBeanInEdition().getDescription().isBlank()
                                                && discountCrudPresenter.getModel().getBeanInEdition().getPercentage() > 0
                                                && discountCrudPresenter.getModel().getBeanInEdition().getStartingDate() != null
                                                && discountCrudPresenter.getModel().getBeanInEdition().getEndingDate() != null
                                                && discountCrudPresenter.getModel().getBeanInEdition().getStartingDate()
                                                    .isBefore(discountCrudPresenter.getModel().getBeanInEdition().getEndingDate()
                                                )){
                                            discountCrudPresenter.getModel().saveItemAction();
                                            product.saveOrUpdate();
                                            discountCrudPresenter.getModel().refreshAction();
                                            discountCrudPresenter.getView().getTableView().getSelectionModel().selectLast();
                                        }else {
                                            DialogBuilder.createError("Error", "SAUL POS", "Empty entity or Invalid Discount percentage or Starting date or Ending date!").showAndWait();
                                        }
                                    }
                                } catch (Exception e) {
                                    DialogBuilder.createExceptionDialog("Exception saving the item", "SAUL POS", e.getMessage(), e).showAndWait();
                                }
                            }
                        }
                );
                AbstractView view = new AbstractView(discountCrudPresenter.getView().getMainView());
                vBox.getChildren().addAll(label, view.getRoot());
                Utils.goForward(new AbstractView(vBox), mainPane);
                return null;
            }
        };

        crudGeneratorParameter.addCustomButton(new CustomButton(discountButtonConstructor, discountButtonFunction, true));
        crudGeneratorParameter.setDataProvider(dataProvider);
        CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        CrudPresenter crud = crudGenerator.generate();
        generateVatComboBox(crudGeneratorParameter, dataProvider);
        viewDef = new AbstractView(crud.getView().getMainView());
        Utils.goForward(viewDef, mainPane);
    }

    private void generateVatComboBox(CrudGeneratorParameter crudGeneratorParameter, HibernateDataProvider dataProvider) {
        PropertySheet propertySheet = (PropertySheet) crudGeneratorParameter.getFieldsLayout().getChildren().getFirst();
        DefaultPropertyEditorFactory defaultPropertyEditorFactory = new DefaultPropertyEditorFactory();
        propertySheet.setPropertyEditorFactory(new Callback<PropertySheet.Item, PropertyEditor<?>>() {
            @Override
            public PropertyEditor<?> call(PropertySheet.Item param) {
                if (param.getName().equals("Vat")){
                    List<Vat> vatList = getVatList(dataProvider);
                    return Editors.createChoiceEditor(param, vatList);
                }
                return defaultPropertyEditorFactory.call(param);
            }
        });
    }

    private List<Vat> getVatList(HibernateDataProvider dataProvider){
        List vats = dataProvider.getAllItems(Vat.class);
        List<Vat> vatList = new ArrayList<>();
        for(Object vat: vats){
            vatList.add((Vat) vat);
        }
        return vatList;
    }
}
