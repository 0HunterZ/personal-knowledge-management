package com.focusnode;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Screen;
import javafx.stage.Stage;

import com.focusnode.service.ServiceLocator;
import java.io.InputStream;

public class Main extends Application {
    private static final double MIN_WIDTH = 1024;
    private static final double MIN_HEIGHT = 700;

    @Override
    public void start(Stage primaryStage) throws Exception {
        ServiceLocator.initialize();

        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        loadFonts();
        // Show local profile creation dialog if missing before loading main UI
        showLocalProfileDialogIfMissing(primaryStage);

        Parent root = FXMLLoader.load(getClass().getResource("/fxml/views/MainLayout.fxml"));
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();

        Scene scene = new Scene(root, visualBounds.getWidth(), visualBounds.getHeight());
        scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());

        loadAppIcon(primaryStage);

        primaryStage.setTitle("Focus-Node");
        primaryStage.setMinWidth(MIN_WIDTH);
        primaryStage.setMinHeight(MIN_HEIGHT);
        primaryStage.setX(visualBounds.getMinX());
        primaryStage.setY(visualBounds.getMinY());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(event -> ServiceLocator.shutdown());
        primaryStage.show();
    }

    private void showLocalProfileDialogIfMissing(Stage owner) {
        try {
            if (!ServiceLocator.getLocalProfileManager().exists()) {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/fxml/dialogs/LocalProfileDialog.fxml"));
                javafx.scene.Parent dlgRoot = loader.load();
                Stage dlg = new Stage();
                dlg.initOwner(owner);
                dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                dlg.setTitle("Thiết lập Hồ sơ Cục bộ");
                dlg.setScene(new javafx.scene.Scene(dlgRoot));
                dlg.showAndWait();
            }
        } catch (Exception e) {
            System.err.println("Failed to show local profile dialog: " + e.getMessage());
        }
    }

    private void loadFonts() {
        loadFont("/fonts/Inter_24pt-Regular.ttf");
        loadFont("/fonts/Inter_24pt-Medium.ttf");
        loadFont("/fonts/Inter_24pt-Bold.ttf");
    }

    private void loadFont(String path) {
        try (InputStream fontStream = getClass().getResourceAsStream(path)) {
            if (fontStream != null) {
                Font.loadFont(fontStream, 14);
            }
        } catch (Exception e) {
            System.out.println("Note: unable to load font " + path + ". Using default system font.");
        }
    }

    private void loadAppIcon(Stage primaryStage) {
        try (InputStream iconStream = getClass().getResourceAsStream("/images/logo.png")) {
            if (iconStream != null) {
                primaryStage.getIcons().add(new Image(iconStream));
            }
        } catch (Exception e) {
            System.out.println("Note: unable to load app icon. Using default system icon.");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
