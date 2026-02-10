package com.saulpos.client.ui.components;

import javafx.scene.control.TableView;

public final class PosTableView<T> extends TableView<T> {

    public PosTableView() {
        super();
        getStyleClass().add("pos-table");
        setFocusTraversable(true);
    }
}
