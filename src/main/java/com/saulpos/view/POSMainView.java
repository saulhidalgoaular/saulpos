package com.saulpos.view;

import com.saulpos.presenter.POSMainPresenter;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;

public class POSMainView extends AbstractView<POSMainPresenter> {

    public POSMainView(String fxmlPath, POSMainPresenter presenter) {
        super(fxmlPath, presenter);
    }

    public POSMainView(Node rootNode) {
        super(rootNode);
    }

    public POSMainView() {
    }

    @Override
    public void initialize() {
        setButtonsIcons("MONEY", presenter.chargeButton);
        setButtonsIcons("MINUS_CIRCLE", presenter.deleteCurrentButton);
        setButtonsIcons("USER", presenter.clientsButton);
        setButtonsIcons("HAND_PAPER_ALT", presenter.removeCashButton);
        setButtonsIcons("CLOCK_ALT", presenter.sendToWaitButton);
        setButtonsIcons("EYE", presenter.viewWaitingButton);
        setButtonsIcons("TRASH", presenter.deleteAllButton);
        setButtonsIcons("FILE_TEXT", presenter.creditNoteButton);
        setButtonsIcons("USD", presenter.globalDiscountButton);
        setButtonsIcons("SIGN_OUT", presenter.exitButton);
        setButtonsIcons("BAR_CHART", presenter.xReportButton);
        setButtonsIcons("BAR_CHART", presenter.zReportButton);
    }

    public void setButtonsIcons(String iconname, Button buttonname) {
        Label iconLabel = POSIcons.getGraphic(iconname);
        buttonname.setGraphic(iconLabel);
        buttonname.setContentDisplay(ContentDisplay.TOP);
    }
}
