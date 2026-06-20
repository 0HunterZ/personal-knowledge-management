package com.focusnode.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class LanDiscoverRoomsController implements Initializable {

    @FXML private VBox discoverRoomsContainer;
    @FXML private VBox emptyStateContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initial empty state
        updateView(com.focusnode.service.ServiceLocator.getLanSessionService().getDiscoveredRooms());

        // Listen for changes
        com.focusnode.service.ServiceLocator.getLanSessionService().getDiscoveredRooms().addListener(
            (javafx.collections.ListChangeListener.Change<? extends com.focusnode.model.LanRoom> c) -> {
                javafx.application.Platform.runLater(() -> {
                    updateView(c.getList());
                });
            }
        );
    }

    private void updateView(javafx.collections.ObservableList<? extends com.focusnode.model.LanRoom> rooms) {
        if (rooms.isEmpty()) {
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
            discoverRoomsContainer.setVisible(false);
            discoverRoomsContainer.setManaged(false);
        } else {
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
            discoverRoomsContainer.setVisible(true);
            discoverRoomsContainer.setManaged(true);
            
            discoverRoomsContainer.getChildren().clear();
            int i = 1;
            for (com.focusnode.model.LanRoom room : rooms) {
                String memberText = room.getMemberCount() + " members";
                discoverRoomsContainer.getChildren().add(createRoomRow(
                    room.getName(), memberText, String.valueOf(i++), "#10B981", "#FFFFFF"
                ));
            }
        }
    }

    private HBox createRoomRow(String name, String members, String id, String signalColor, String bgColor) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-background-color: " + bgColor + "; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #F1F5F9; -fx-border-radius: 8;");
        
        Label idLabel = new Label(id);
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1F2937;");
        
        VBox info = new VBox(2);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1F2937;");
        Label memberLabel = new Label(members);
        memberLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        info.getChildren().addAll(nameLabel, memberLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label signalIcon = new Label("📶");
        signalIcon.setStyle("-fx-text-fill: " + signalColor + "; -fx-font-size: 14px;");
        
        row.getChildren().addAll(idLabel, info, spacer, signalIcon);
        
        if (bgColor.equals("#FFFFFF")) {
            row.setOnMouseEntered(e -> row.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #F1F5F9; -fx-border-radius: 8;"));
            row.setOnMouseExited(e -> row.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 12 15; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-color: #F1F5F9; -fx-border-radius: 8;"));
        }
        
        return row;
    }
}
