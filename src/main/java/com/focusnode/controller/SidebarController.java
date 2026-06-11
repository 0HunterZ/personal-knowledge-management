package com.focusnode.controller;

import com.focusnode.navigation.AppNavigator;
import com.focusnode.navigation.AppView;
import javafx.fxml.FXML;

public class SidebarController {
    @FXML
    private void showHome() {
        AppNavigator.navigateTo(AppView.HOME);
    }

    @FXML
    private void showKnowledge() {
        AppNavigator.navigateTo(AppView.KNOWLEDGE);
    }

    @FXML
    private void showLanHub() {
        AppNavigator.navigateTo(AppView.LAN_HUB);
    }

    @FXML
    private void showTasks() {
        AppNavigator.navigateTo(AppView.TASKS);
    }

    @FXML
    private void showZenMode() {
        AppNavigator.navigateTo(AppView.ZEN_MODE);
    }

    @FXML
    private void showTags() {
        AppNavigator.navigateTo(AppView.TAGS);
    }

    @FXML
    private void showAnalytics() {
        AppNavigator.navigateTo(AppView.ANALYTICS);
    }

    @FXML
    private void showReview() {
        AppNavigator.navigateTo(AppView.REVIEW);
    }
}
