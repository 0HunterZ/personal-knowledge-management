package com.focusnode.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class LanActiveRoomController implements Initializable {

    @FXML private VBox activeStateContainer;
    @FXML private VBox emptyStateContainer;

    @FXML private Label roomTitleLabel;
    @FXML private Label roomIpLabel;
    @FXML private Label memberCountLabel;
    @FXML private Label leaveBtn;
    @FXML private VBox membersContainer;
    @FXML private VBox activityContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initial state
        updateView(com.focusnode.service.ServiceLocator.getLanSessionService().getActiveRoom());

        // Listen for active room changes
        com.focusnode.service.ServiceLocator.getLanSessionService().activeRoomProperty().addListener(
            (obs, oldRoom, newRoom) -> {
                javafx.application.Platform.runLater(() -> {
                    updateView(newRoom);
                });
            }
        );

        if (leaveBtn != null) {
            leaveBtn.setOnMouseClicked(e -> {
                com.focusnode.service.LanSessionService service = com.focusnode.service.ServiceLocator.getLanSessionService();
                if (service.getLocalMember() != null) {
                    service.leaveRoom(service.getLocalMember());
                }
            });
        }
    }
    
    private void updateView(com.focusnode.model.LanRoom room) {
        if (room == null) {
            emptyStateContainer.setVisible(true);
            emptyStateContainer.setManaged(true);
            activeStateContainer.setVisible(false);
            activeStateContainer.setManaged(false);
        } else {
            emptyStateContainer.setVisible(false);
            emptyStateContainer.setManaged(false);
            activeStateContainer.setVisible(true);
            activeStateContainer.setManaged(true);
            
            roomTitleLabel.setText(room.getName());
            roomIpLabel.setText("IP: " + room.getHostIp() + "  •  Port: " + room.getPort());
            
            // Re-render members
            renderRealMembers(room);
            renderActivities(room);

            // Listen for member list changes inside this room
            room.getMembers().addListener((javafx.collections.ListChangeListener.Change<? extends com.focusnode.model.LanMember> c) -> {
                javafx.application.Platform.runLater(() -> {
                    renderRealMembers(room);
                });
            });

            // Listen for activities changes
            room.getActivities().addListener((javafx.collections.ListChangeListener.Change<? extends com.focusnode.model.LanActivity> c) -> {
                javafx.application.Platform.runLater(() -> {
                    renderActivities(room);
                });
            });
        }
    }

    private void renderRealMembers(com.focusnode.model.LanRoom room) {
        if (membersContainer == null) return;
        membersContainer.getChildren().clear();
        
        memberCountLabel.setText("Participants (" + room.getMembers().size() + "/8)");

        String[] colors = {"avatar-green", "avatar-purple", "avatar-yellow", "avatar-blue"};
        int colorIdx = 0;

        for (com.focusnode.model.LanMember member : room.getMembers()) {
            String initial = member.getName().isEmpty() ? "?" : member.getName().substring(0, 1).toUpperCase();
            String name = member.getName();
            if (member.isHost()) name += " 👑";
            
            String status = member.getStatus() != null ? member.getStatus() : "Idle";
            String statusClass = status.equals("Focusing") ? "status-focusing" : (status.equals("Break") ? "status-break" : "status-idle");
            
            long timeLeft = member.getTimeLeftSeconds();
            String timeStr = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
            
            String audioIcon = member.isAudioMuted() ? "🔇" : "🔊";
            
            membersContainer.getChildren().add(createMemberRow(
                initial, name, member.getIpAddress(), colors[colorIdx % colors.length], 
                status, statusClass, timeStr, audioIcon
            ));
            colorIdx++;
        }
    }

    private HBox createMemberRow(String initial, String name, String ip, String avatarClass, String status, String statusClass, String time, String audioIcon) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setStyle("-fx-padding: 8 0; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");
        
        Label avatar = new Label(initial);
        avatar.getStyleClass().addAll("member-avatar", avatarClass);
        
        VBox info = new VBox(2);
        info.setPrefWidth(120);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: " + (name.contains("You") ? "#10B981" : "#1F2937") + ";");
        Label ipLabel = new Label(ip);
        ipLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        info.getChildren().addAll(nameLabel, ipLabel);
        
        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);
        
        Label statusBadge = new Label(status);
        statusBadge.getStyleClass().add(statusClass);
        statusBadge.setPrefWidth(70);
        statusBadge.setAlignment(Pos.CENTER);
        
        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #475569;");
        timeLabel.setPrefWidth(40);
        
        Label audioLabel = new Label(audioIcon);
        audioLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #94A3B8;");
        
        row.getChildren().addAll(avatar, info, spacer1, statusBadge, spacer2, timeLabel, audioLabel);
        return row;
    }

    private void renderActivities(com.focusnode.model.LanRoom room) {
        if (activityContainer == null) return;
        activityContainer.getChildren().clear();

        for (com.focusnode.model.LanActivity act : room.getActivities()) {
            activityContainer.getChildren().add(createActivityRow(act.getFormattedTime(), act.getColorHex(), act.getText()));
        }
    }

    private HBox createActivityRow(String time, String dotColor, String text) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        
        Label timeLabel = new Label(time);
        timeLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        
        Circle dot = new Circle(3);
        dot.setStyle("-fx-fill: " + dotColor + ";");
        
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: " + (dotColor.equals("#10B981") ? "#10B981" : "#475569") + ";");
        
        row.getChildren().addAll(timeLabel, dot, textLabel);
        return row;
    }
}
