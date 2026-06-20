package com.focusnode.navigation;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public final class AppNavigator {
    private static BorderPane rootPane;
    private static Node sidebarArea;
    private static StackPane contentArea;
    private static String defaultRootStyle;

    private AppNavigator() {
    }

    public static void setContentArea(StackPane contentArea) {
        AppNavigator.contentArea = contentArea;
    }

    public static void setShell(BorderPane rootPane, Node sidebarArea) {
        AppNavigator.rootPane = rootPane;
        AppNavigator.sidebarArea = sidebarArea;
        AppNavigator.defaultRootStyle = rootPane.getStyle();
    }

    public static void navigateTo(AppView view) {
        if (contentArea == null) {
            throw new IllegalStateException("Content area has not been initialized.");
        }

        try {
            Node screen = FXMLLoader.load(AppNavigator.class.getResource(view.getFxmlPath()));
            contentArea.getChildren().setAll(screen);
            updateShellFor(view);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load view: " + view.getFxmlPath(), e);
        }
    }

    private static void updateShellFor(AppView view) {
        boolean zenMode = view == AppView.ZEN_MODE;

        if (rootPane != null) {
            rootPane.setLeft(zenMode ? null : sidebarArea);
            rootPane.setStyle(zenMode ? "-fx-padding: 0; -fx-background-color: #F8FAFC;" : defaultRootStyle);
        }

        if (contentArea != null) {
            contentArea.setStyle(zenMode ? "-fx-background-color: #111827;" : "-fx-background-color: #F8FAFC;");
            applyFullScreen(zenMode);
            Platform.runLater(() -> applyFullScreen(zenMode));
        }
    }

    private static void applyFullScreen(boolean fullScreen) {
        if (contentArea == null || contentArea.getScene() == null || contentArea.getScene().getWindow() == null) {
            return;
        }

        if (contentArea.getScene().getWindow() instanceof Stage stage) {
            stage.setFullScreenExitHint("");
            stage.setFullScreen(fullScreen);
        }
    }
}
