package com.saulpos.model.menu.action;

import com.saulpos.javafxcrudgenerator.CrudGenerator;
import com.saulpos.javafxcrudgenerator.CrudGeneratorParameter;
import com.saulpos.javafxcrudgenerator.model.Function;
import com.saulpos.javafxcrudgenerator.presenter.CrudPresenter;
import com.saulpos.javafxcrudgenerator.sample.Product;
import com.saulpos.javafxcrudgenerator.view.CustomButton;
import com.saulpos.javafxcrudgenerator.view.NodeConstructor;
import com.saulpos.model.bean.Cashier;
import com.saulpos.model.bean.MenuModel;
import com.saulpos.model.bean.Profile;
import com.saulpos.model.bean.Shift;
import com.saulpos.model.dao.HibernateDataProvider;
import com.saulpos.view.Utils;
import com.saulpos.view.menu.CheckBoxTreeItemMenuGenerator;
import de.jensd.fx.glyphs.GlyphsDude;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;

public class ManageProfileMenuAction extends CrudMenuAction{

    public ManageProfileMenuAction() {
        super(Profile.class);
    }

    @Override
    public Object run(final ArrayList<MenuModel> completeMenu, Pane mainPane) throws Exception {
        CrudGeneratorParameter crudGeneratorParameter = new CrudGeneratorParameter();
        crudGeneratorParameter.setClazz(this.crudClass);
        HibernateDataProvider dataProvider = new HibernateDataProvider();

        NodeConstructor customButtonConstructor = new NodeConstructor() {
            @Override
            public Node generateNode(Object... name) {
                Button customButton = new Button();
                Label icon = GlyphsDude.createIconLabel(FontAwesomeIcon.BOOK, crudGeneratorParameter.translate("permissions"), "20px", "10px", ContentDisplay.LEFT);
                customButton.setGraphic(icon);
                customButton.setPrefWidth(crudGeneratorParameter.getButtonWidth());
                return customButton;
            }
        };

        Function customButtonFunction = new Function() {
            @Override
            public Object[] run(Object[] params) throws Exception {

                Label label = new Label("Select known languages:");

                VBox vBox = new VBox(5);
                vBox.setPadding(new Insets(30));
                vBox.getChildren().addAll(label, CheckBoxTreeItemMenuGenerator.generateMenuNode(completeMenu));

                Utils.goForward(new Utils.ViewDef(vBox), mainPane);

                Profile productBeingEdited = (Profile) params[0];
                System.out.println(productBeingEdited.getDescription());
                return null;
            }
        };

        crudGeneratorParameter.addCustomButton(new CustomButton(customButtonConstructor, customButtonFunction, true));
        crudGeneratorParameter.setDataProvider(dataProvider);
        CrudGenerator crudGenerator = new CrudGenerator<>(crudGeneratorParameter);
        CrudPresenter crud = crudGenerator.generate();
        Utils.goForward(new Utils.ViewDef(crud.getView().getMainView()), mainPane);
        return null;
    }
}
