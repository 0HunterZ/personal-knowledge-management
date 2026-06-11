package com.focusnode;

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

public class TestFXML {
    public static void main(String[] args) {
        Platform.startup(() -> {
            try {
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
                File dir = new File("screenshots");
                if (!dir.exists()) dir.mkdirs();

                for (AppView view : AppView.values()) {
                    System.out.println("Processing " + view.name());
                    FXMLLoader loader = new FXMLLoader(TestFXML.class.getResource(view.getFxmlPath()));
                    javafx.scene.Parent root = loader.load();
                    Scene scene = new Scene(root, 1280, 800);
                    try { scene.getStylesheets().add(TestFXML.class.getResource("/css/styles.css").toExternalForm()); } catch(Exception e) {}
                    
                    Stage stage = new Stage();
                    stage.setScene(scene);
                    stage.show();
                    
                    root.applyCss();
                    root.layout();
                    
                    WritableImage img = root.snapshot(new javafx.scene.SnapshotParameters(), null);
                    ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", new File(dir, view.name() + ".png"));
                    System.out.println("Saved " + view.name());
                    stage.close();
                }
                System.out.println("DONE");
                System.exit(0);
            } catch(Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
        });
    }
}
