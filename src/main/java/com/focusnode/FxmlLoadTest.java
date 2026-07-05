package com.focusnode;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import java.io.File;
import java.net.URL;

public class FxmlLoadTest {
    public static void main(String[] args) throws Exception {
        // We must initialize JavaFX Toolkit before loading FXMLs that might have UI controls
        Platform.startup(() -> {});

        File viewsDir = new File("src/main/resources/fxml/views");
        if (!viewsDir.exists() || !viewsDir.isDirectory()) {
            System.err.println("Views directory not found: " + viewsDir.getAbsolutePath());
            System.exit(1);
        }

        boolean allPassed = true;
        for (File fxmlFile : viewsDir.listFiles()) {
            if (fxmlFile.getName().endsWith(".fxml")) {
                try {
                    URL url = fxmlFile.toURI().toURL();
                    FXMLLoader loader = new FXMLLoader(url);
                    Parent root = loader.load();
                    System.out.println("[SUCCESS] Loaded " + fxmlFile.getName());
                } catch (Exception e) {
                    System.err.println("[ERROR] Failed to load " + fxmlFile.getName());
                    e.printStackTrace();
                    allPassed = false;
                }
            }
        }

        // Test components too
        File componentsDir = new File("src/main/resources/fxml/components");
        if (componentsDir.exists() && componentsDir.isDirectory()) {
            for (File fxmlFile : componentsDir.listFiles()) {
                if (fxmlFile.getName().endsWith(".fxml")) {
                    try {
                        URL url = fxmlFile.toURI().toURL();
                        FXMLLoader loader = new FXMLLoader(url);
                        Parent root = loader.load();
                        System.out.println("[SUCCESS] Loaded " + fxmlFile.getName());
                    } catch (Exception e) {
                        System.err.println("[ERROR] Failed to load " + fxmlFile.getName());
                        e.printStackTrace();
                        allPassed = false;
                    }
                }
            }
        }

        Platform.exit();
        System.exit(allPassed ? 0 : 1);
    }
}
