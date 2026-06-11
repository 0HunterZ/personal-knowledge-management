package com.focusnode.util;

import com.focusnode.navigation.AppView;
import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.WritableImage;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.InputStream;
import javafx.scene.text.Font;
import java.util.concurrent.CountDownLatch;

public class ScreenshotTool extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
        try {
            InputStream f1 = getClass().getResourceAsStream("/fonts/Inter_24pt-Regular.ttf");
            if (f1 != null) Font.loadFont(f1, 14);
        } catch(Exception e) {}

        File dir = new File("screenshots");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        new Thread(() -> {
            for (AppView view : AppView.values()) {
                try {
                    CountDownLatch latch = new CountDownLatch(1);
                    Platform.runLater(() -> {
                        try {
                            System.out.println("Taking screenshot of " + view.name() + "...");
                            FXMLLoader loader = new FXMLLoader(getClass().getResource(view.getFxmlPath()));
                            javafx.scene.Parent root = loader.load();
                            Scene scene = new Scene(root, 1280, 800);
                            try { scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm()); } catch(Exception e){}
                            
                            Stage stage = new Stage();
                            stage.setScene(scene);
                            stage.show();
                            
                            new Thread(() -> {
                                try { Thread.sleep(500); } catch(Exception e){}
                                Platform.runLater(() -> {
                                    try {
                                        WritableImage image = root.snapshot(new javafx.scene.SnapshotParameters(), null);
                                        File file = new File(dir, view.name() + ".png");
                                        ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", file);
                                        System.out.println("Saved " + file.getAbsolutePath());
                                    } catch(Exception e) { e.printStackTrace(); }
                                    stage.close();
                                    latch.countDown();
                                });
                            }).start();

                        } catch (Exception e) {
                            System.err.println("Failed on view " + view.name() + ": " + e.getMessage());
                            latch.countDown();
                        }
                    });
                    latch.await();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Done capturing screenshots!");
            System.exit(0);
        }).start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
