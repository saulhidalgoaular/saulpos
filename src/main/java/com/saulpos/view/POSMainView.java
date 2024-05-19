package com.saulpos.view;

import com.saulpos.presenter.AbstractPresenter;
import javafx.scene.Node;

public class POSMainView extends AbstractView{

    public POSMainView(String fxmlPath, AbstractPresenter presenter) {
        super(fxmlPath, presenter);
    }

    public POSMainView(Node rootNode) {
        super(rootNode);
    }

    public POSMainView() {
    }

    @Override
    public void initialize() {

    }
}
