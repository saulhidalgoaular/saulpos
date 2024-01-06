package com.saulpos.presenter;

import com.saulpos.model.POSMainModel;
import com.saulpos.view.POSMainView;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class POSMainPresenter extends AbstractPresenter<POSMainModel, POSMainView> {

    // Create the FXML components to bind
    @FXML
    public VBox mainPOSVBox;

    public POSMainPresenter(POSMainModel model, POSMainView view) {
        super(model, view);
    }

    @Override
    public void addBinding() {

    }

    @Override
    public void addComponents() {

    }

    @Override
    public void initializeComponents() {

    }

    @Override
    public void entryActions() {

    }
}
