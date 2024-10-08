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
import com.saulpos.javafxcrudgenerator.model.dao.AbstractDataProvider;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.view.*;
import com.saulpos.model.MainModel;
import com.saulpos.model.bean.Discount;
import com.saulpos.model.bean.Price;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

        Function customButtonFunction = new Function() {
            @Override
            public Object[] run(Object[] params) throws Exception {
                Product product = (Product)params[0];

                Label label = new Label("Assign the price for the product:");

                VBox vBox = new VBox(5);
                vBox.setPadding(new Insets(30));


                CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();
                crudGeneratorParameter.setClazz(Price.class);

                HibernateDataProvider dataProviderForColumn = new HibernateDataProvider(){

                    @Override
                    public List getAllItems(Class aClass, AbstractBean abstractBean, SearchType type) {
                        Price dummyPrice = new Price();
                        dummyPrice.setProduct(product);

                        try {
                            return DatabaseConnection.getInstance().listBySample(Price.class, dummyPrice, SearchType.EQUAL);
                        } catch (Exception e) {
                            DialogBuilder.createExceptionDialog("Error", "Error query the database", e.getMessage(), e).showAndWait();
                        }

                        return new ArrayList();
                    }
                };


                // Custom Buttons
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
                        if (viewDef != null) {
                            Utils.goBack(viewDef, mainPane);
                        }
                        return null;
                    }
                };


                crudGeneratorParameter.addCustomButton(new CustomButton(customBackButtonConstructor, backButtonFunction, false));


                crudGeneratorParameter.setDataProvider(dataProviderForColumn);

                // FIXME later this is not too elegant, but let's leave it for now...
                CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter){
                    @Override
                    public CrudPresenter generate() throws Exception {
                        final CrudModel model = new CrudModel<>(crudGeneratorParameter){
                            @Override
                            public AbstractBean getNewBean() {
                                Price newBean = new Price();
                                newBean.setProduct(product);
                                return newBean;
                            }
                        };


                        final CrudView view = new CrudViewGenerator(crudGeneratorParameter).generate();

                        return new CrudPresenter (model, view);
                    }
                };

                CrudPresenter crud = crudGenerator.generate();

                ((Button)crud.getView().getSaveButton()).setOnAction(
                    new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent actionEvent) {
                            try {
                                crud.getModel().saveItemAction();
                                // Let's select the item in case it was a new one.

                                if (crud.getView().getTableView().getSelectionModel().selectedItemProperty().get() == null){
                                    crud.getView().getTableView().getSelectionModel().selectLast();
                                }


                                // FIXME We are querying the database twice, it is not ideal, but let's leave it like this for now

                                List allItems = dataProvider.getAllItems(Product.class, product, AbstractDataProvider.SearchType.EQUAL);
                                Set<Price> set = ((Product) allItems.get(0)).getPriceList();
                                product.setPriceList(set);
                                product.saveOrUpdate();
                            } catch (Exception e) {
                                DialogBuilder.createExceptionDialog("Exception saving the item", "SAUL POS", e.getMessage(), e).showAndWait();
                            }
                        }
                    }
                );


                AbstractView view = new AbstractView(crud.getView().getMainView());


                vBox.getChildren().addAll(label, view.getRoot());

                Utils.goForward(new AbstractView(vBox), mainPane);
                return null;
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
                                        if(discountCrudPresenter.getModel().getBeanInEdition().getDescription() != null
                                                && discountCrudPresenter.getModel().getBeanInEdition().getPercentage() > 0
                                                && discountCrudPresenter.getModel().getBeanInEdition().getStartingDate() != null
                                                && discountCrudPresenter.getModel().getBeanInEdition().getEndingDate() != null
                                                && discountCrudPresenter.getModel().getBeanInEdition().getStartingDate()
                                                    .isBefore(discountCrudPresenter.getModel().getBeanInEdition().getEndingDate()
                                                )){
                                            discountCrudPresenter.getModel().saveItemAction();
                                            product.setDiscount((Discount) dataProvider.getAllItems(Discount.class).getLast());
                                            product.saveOrUpdate();
                                            discountCrudPresenter.getModel().refreshAction();
                                            discountCrudPresenter.getView().getTableView().getSelectionModel().selectLast();
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

        NodeConstructor vatButtonConstructor = new NodeConstructor() {
            @Override
            public Node generateNode(Object... objects) {
                Button vatBtn = new Button();
                Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.PERCENT, crudGeneratorParameter.translate("Vat"), "20px", "10px", ContentDisplay.LEFT);
                vatBtn.setGraphic(icon);
                vatBtn.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                return vatBtn;
            }
        };
        Function vatButtonFunction = new Function() {
            @Override
            public Object[] run(Object[] objects) throws Exception {
                Product product = (Product) objects[0];
                TextFlow textFlow = new TextFlow();
                Text text1=new Text("Assign the vat for the product: ");
                Text text2=new Text(product.getDescription());
                text2.setStyle("-fx-font-weight: bold");
                textFlow.getChildren().addAll(text1, text2);

                VBox vBox = new VBox(5);
                vBox.setPadding(new Insets(30));

                CrudGeneratorParameter<Vat> crudVatParamGenerator = new CrudGeneratorParameter<>();
                crudVatParamGenerator.setClazz(Vat.class);

                HibernateDataProvider vatDataProvider = new HibernateDataProvider(){
                    @Override
                    public List getAllItems(Class aClass, AbstractBean abstractBean, SearchType type) {
                        Vat vat = new Vat();
                        vat.setProduct(product);
                        try {
                            List list = DatabaseConnection.getInstance().listBySample(Vat.class, vat, SearchType.EQUAL);
                            return list;
                        } catch (Exception e) {
                            DialogBuilder.createExceptionDialog("Error", "Error query the database", e.getMessage(), e).showAndWait();
                        }
                        return new ArrayList<>();
                    }
                };

                NodeConstructor vatBackButtonConstructor = new NodeConstructor() {
                    @Override
                    public Node generateNode(Object... objects) {
                        Button customVatBackButton = new Button();
                        Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.BACKWARD, crudVatParamGenerator.translate("Back"), "20px", "10px", ContentDisplay.LEFT);
                        customVatBackButton.setGraphic(icon);
                        customVatBackButton.setPrefWidth(crudVatParamGenerator.getButtonWidth());
                        return customVatBackButton;
                    }
                };
                Function vatBackButtonFunction = new Function() {
                    @Override
                    public Object[] run(Object[] objects) throws Exception {
                        if(viewDef != null){
                            Utils.goBack(viewDef, mainPane);
                        }
                        return null;
                    }
                };
                crudVatParamGenerator.addCustomButton(new CustomButton(vatBackButtonConstructor, vatBackButtonFunction, false));
                crudVatParamGenerator.setDataProvider(vatDataProvider);

                CrudGenerator<Vat> crudVatGenerator = new CrudGenerator<>(crudVatParamGenerator){
                    @Override
                    public CrudPresenter<Vat> generate() throws Exception {
                        final CrudModel<Vat> model = new CrudModel<>(crudVatParamGenerator){
                            @Override
                            public Vat getNewBean() {
                                Vat newBean = new Vat();
                                product.setVat(newBean);
                                return newBean;
                            }
                        };
                        final CrudView view = new CrudViewGenerator(crudVatParamGenerator).generate();
                        return new CrudPresenter<Vat>(model, view);
                    }
                };
                CrudPresenter<Vat> vatCrudPresenter = crudVatGenerator.generate();

                ((Button) vatCrudPresenter.getView().getSaveButton()).setOnAction(
                        new EventHandler<ActionEvent>() {
                            @Override
                            public void handle(ActionEvent actionEvent) {
                                try{
                                    if(vatCrudPresenter.getView().getTableView().getItems().size() > 0 &&
                                        vatCrudPresenter.getView().getTableView().getSelectionModel().getSelectedItem() ==null){
                                        DialogBuilder.createError("Error", "SAUL POS",
                                                "Adding multiple vat information for a single product is not allowed!").showAndWait();
                                    }else{
                                        //Condition for not to add the empty vat details
                                        if(vatCrudPresenter.getModel().getBeanInEdition().getDescription() != null
                                                && vatCrudPresenter.getModel().getBeanInEdition().getPercentage() > 0){
                                            vatCrudPresenter.getModel().saveItemAction();
                                            product.setVat((Vat) dataProvider.getAllItems(Vat.class).getLast());
                                            product.saveOrUpdate();
                                            vatCrudPresenter.getModel().refreshAction();
                                            vatCrudPresenter.getView().getTableView().getSelectionModel().selectLast();
                                        }
                                    }
                                } catch (Exception e) {
                                    DialogBuilder.createExceptionDialog("Exception saving the item", "SAUL POS", e.getMessage(), e).showAndWait();
                                }
                            }
                        }
                );

                AbstractView view = new AbstractView(vatCrudPresenter.getView().getMainView());
                vBox.getChildren().addAll(textFlow, view.getRoot());
                Utils.goForward(new AbstractView(vBox), mainPane);
                return null;
            }
        };

        crudGeneratorParameter.addCustomButton(new CustomButton(customButtonConstructor, customButtonFunction, true));
        crudGeneratorParameter.addCustomButton(new CustomButton(discountButtonConstructor, discountButtonFunction, true));
        crudGeneratorParameter.addCustomButton(new CustomButton(vatButtonConstructor, vatButtonFunction, true));
        crudGeneratorParameter.setDataProvider(dataProvider);
        CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        CrudPresenter crud = crudGenerator.generate();

        viewDef = new AbstractView(crud.getView().getMainView());
        Utils.goForward(viewDef, mainPane);
    }
}
