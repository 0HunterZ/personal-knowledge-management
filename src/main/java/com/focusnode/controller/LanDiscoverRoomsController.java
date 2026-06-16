package com.focusnode.controller;

import com.focusnode.model.LanRoom;
import com.focusnode.service.LanSessionService;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.net.URL;
import java.util.ResourceBundle;

public class LanDiscoverRoomsController implements Initializable {

    @FXML private VBox discoverRoomsContainer;

    private final LanSessionService sessionService = ServiceLocator.getLanSessionService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initial render
        renderRooms();

        // Listen for changes in discovered rooms
        sessionService.getDiscoveredRooms().addListener((ListChangeListener<LanRoom>) change -> {
            Platform.runLater(this::renderRooms);
        });
    }

    private void renderRooms() {
        discoverRoomsContainer.getChildren().clear();

        if (sessionService.getDiscoveredRooms().isEmpty()) {
            Label empty = new Label("No rooms found on LAN.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-padding: 10;");
            discoverRoomsContainer.getChildren().add(empty);
            return;
        }

        for (LanRoom room : sessionService.getDiscoveredRooms()) {
            discoverRoomsContainer.getChildren().add(createRoomRow(room));
        }
    }

    private HBox createRoomRow(LanRoom room) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;");
        
        // Hover spell
        row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F1F5F9; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 10; -fx-background-radius: 8; -fx-cursor: hand;"));

        // Member Count Badge
        Label countLabel = new Label();
        countLabel.textProperty().bind(room.memberCountProperty().asString());
        countLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1F2937;");

        // Info
        VBox info = new VBox(2);
        Label nameLabel = new Label(room.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1F2937;");
        
        Label membersText = new Label();
        membersText.textProperty().bind(room.memberCountProperty().asString().concat("/").concat(room.maxMembersProperty().asString()).concat(" members"));
        membersText.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        
        info.getChildren().addAll(nameLabel, membersText);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Signal Bars
        HBox signalBars = new HBox(2);
        signalBars.setAlignment(Pos.BOTTOM_CENTER);
        
        // Mock signal strength (3 bars)
        Rectangle b1 = new Rectangle(3, 4, javafx.scene.paint.Color.web("#22C55E"));
        Rectangle b2 = new Rectangle(3, 8, javafx.scene.paint.Color.web("#22C55E"));
        Rectangle b3 = new Rectangle(3, 12, javafx.scene.paint.Color.web("#22C55E"));
        Rectangle b4 = new Rectangle(3, 16, javafx.scene.paint.Color.web("#E2E8F0")); // Empty
        
        signalBars.getChildren().addAll(b1, b2, b3, b4);

        row.getChildren().addAll(countLabel, info, spacer, signalBars);
        return row;
    }
}
