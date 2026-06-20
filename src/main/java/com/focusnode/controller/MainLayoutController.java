package com.focusnode.controller;

import com.focusnode.navigation.AppNavigator;
import com.focusnode.navigation.AppView;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;

public class MainLayoutController {
    @FXML
    private BorderPane rootPane;

    @FXML
    private StackPane sidebarArea;

    @FXML
    private StackPane contentArea;

    @FXML
    private void initialize() {
        AppNavigator.setShell(rootPane, sidebarArea);
        AppNavigator.setContentArea(contentArea);
        AppNavigator.navigateTo(AppView.HOME);
    }
}
