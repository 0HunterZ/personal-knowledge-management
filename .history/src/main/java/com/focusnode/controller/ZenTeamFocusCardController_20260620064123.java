package com.focusnode.controller;

import com.focusnode.model.LanMember;
import com.focusnode.model.LanRoom;
import com.focusnode.service.LanSessionService;
import com.focusnode.service.ServiceLocator;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.net.URL;
import java.util.ResourceBundle;

public class ZenTeamFocusCardController implements Initializable {

    @FXML private VBox membersContainer;
    @FXML private Button viewAllButton;

    private final LanSessionService lanSessionService = ServiceLocator.getLanSessionService();
    private final ListChangeListener<LanMember> memberListListener = change -> renderMembers();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (viewAllButton != null) {
            viewAllButton.setOnAction(event -> com.focusnode.navigation.AppNavigator.navigateTo(com.focusnode.navigation.AppView.LAN_HUB));
        }

        lanSessionService.activeRoomProperty().addListener((obs, oldRoom, newRoom) -> {
            if (oldRoom != null) {
                oldRoom.getMembers().removeListener(memberListListener);
            }
            if (newRoom != null) {
                newRoom.getMembers().addListener(memberListListener);
            }
            renderMembers();
        });

        LanRoom activeRoom = lanSessionService.getActiveRoom();
        if (activeRoom != null) {
            activeRoom.getMembers().addListener(memberListListener);
        }

        renderMembers();
    }

    private void renderMembers() {
        if (membersContainer == null) {
            return;
        }

        membersContainer.getChildren().clear();
        LanRoom activeRoom = lanSessionService.getActiveRoom();
        if (activeRoom == null || activeRoom.getMembers().isEmpty()) {
            Label empty = new Label("No team connected yet.");
            empty.getStyleClass().add("zen-placeholder-text");
            membersContainer.getChildren().add(empty);
            return;
        }

        for (LanMember member : activeRoom.getMembers()) {
            membersContainer.getChildren().add(createMemberRow(member));
        }
    }

    private HBox createMemberRow(LanMember member) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("zen-team-member-row");

        Circle statusDot = new Circle(7);
        statusDot.getStyleClass().add(member.isHost() ? "zen-member-host-dot" : "zen-member-dot");
        if ("Break".equalsIgnoreCase(member.getStatus())) {
            statusDot.getStyleClass().add("zen-status-break-dot");
        } else {
            statusDot.getStyleClass().add("zen-status-focus-dot");
        }

        Label nameLabel = new Label(member.getName() + (member.isHost() ? " (Host)" : ""));
        nameLabel.getStyleClass().add("zen-member-name");

        Label statusLabel = new Label(member.getStatus());
        statusLabel.getStyleClass().add("zen-status-label");
        if ("Break".equalsIgnoreCase(member.getStatus())) {
            statusLabel.getStyleClass().add("zen-status-break");
        } else {
            statusLabel.getStyleClass().add("zen-status-focus");
        }

        Label timerLabel = new Label(formatTime(member.getTimeLeftSeconds()));
        timerLabel.getStyleClass().add("zen-member-time");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        row.getChildren().addAll(statusDot, nameLabel, spacer, statusLabel, timerLabel);
        return row;
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remaining = seconds % 60;
        return String.format("%02d:%02d", minutes, remaining);
    }
}
