package com.focusnode.controller;

import com.focusnode.model.LanMember;
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
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class LanActiveRoomController implements Initializable {

    @FXML private Label roomTitleLabel;
    @FXML private Label roomIpLabel;
    @FXML private Label memberCountLabel;
    @FXML private VBox membersContainer;

    private final LanSessionService sessionService = ServiceLocator.getLanSessionService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        sessionService.activeRoomProperty().addListener((obs, oldRoom, newRoom) -> {
            updateRoomView(newRoom);
        });

        // Initialize with current state
        updateRoomView(sessionService.getActiveRoom());
    }

    private void updateRoomView(LanRoom room) {
        if (room == null) {
            // Show empty state
            roomTitleLabel.setText("Not in a room");
            roomIpLabel.setText("-");
            memberCountLabel.setText("0");
            membersContainer.getChildren().clear();
            return;
        }

        roomTitleLabel.setText(room.getName());
        roomIpLabel.setText("IP: " + room.getHostIp() + "  •  Port: " + room.getPort());
        
        // Bind member count
        memberCountLabel.textProperty().bind(room.memberCountProperty().asString());

        // Update members list
        renderMembers(room);

        // Listen for member changes
        room.getMembers().addListener((ListChangeListener<LanMember>) change -> {
            Platform.runLater(() -> renderMembers(room));
        });
    }

    private void renderMembers(LanRoom room) {
        membersContainer.getChildren().clear();
        
        for (int i = 0; i < room.getMembers().size(); i++) {
            LanMember member = room.getMembers().get(i);
            boolean isLast = (i == room.getMembers().size() - 1);
            membersContainer.getChildren().add(createMemberRow(member, isLast));
        }
    }

    private HBox createMemberRow(LanMember member, boolean isLast) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add(isLast ? "lan-member-row-last" : "lan-member-row");

        // Avatar
        Label avatar = new Label(member.getName().substring(0, 1).toUpperCase());
        avatar.getStyleClass().addAll("member-avatar");
        if (member.isHost()) {
            avatar.getStyleClass().add("lan-avatar-green");
        } else {
            avatar.getStyleClass().add("lan-avatar-purple"); // Randomize later
        }

        // Info
        VBox info = new VBox();
        Label nameLabel = new Label(member.getName() + (member.isHost() ? " 👑" : ""));
        nameLabel.getStyleClass().add(member.isHost() ? "lan-member-name-host" : "lan-member-name");
        
        Label ipLabel = new Label(member.getIpAddress());
        ipLabel.getStyleClass().add("lan-member-ip");
        info.getChildren().addAll(nameLabel, ipLabel);

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status
        Label statusLabel = new Label();
        statusLabel.textProperty().bind(member.statusProperty());
        statusLabel.getStyleClass().add("status-focusing"); // Default
        member.statusProperty().addListener((obs, old, newVal) -> {
            statusLabel.getStyleClass().removeAll("status-focusing", "status-break");
            statusLabel.getStyleClass().add("Break".equals(newVal) ? "status-break" : "status-focusing");
        });

        // Time
        Label timeLabel = new Label();
        timeLabel.getStyleClass().add("lan-member-time");
        timeLabel.textProperty().bind(member.timeLeftSecondsProperty().map(seconds -> {
            long m = seconds.longValue() / 60;
            long s = seconds.longValue() % 60;
            return String.format("%02d:%02d", m, s);
        }));

        // Audio icon
        Label audioIcon = new Label();
        audioIcon.textProperty().bind(member.isAudioMutedProperty().map(muted -> muted ? "🔇" : "🔊"));
        audioIcon.getStyleClass().add("lan-audio-muted");

        row.getChildren().addAll(avatar, info, spacer, statusLabel, timeLabel, audioIcon);
        return row;
    }
}
