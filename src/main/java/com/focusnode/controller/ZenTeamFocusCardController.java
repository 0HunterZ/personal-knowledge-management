package com.focusnode.controller;

import com.focusnode.model.LanMember;
import com.focusnode.model.LanRoom;
import com.focusnode.model.PomodoroSyncState;
import com.focusnode.service.LanSessionService;
import com.focusnode.service.ServiceLocator;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ZenTeamFocusCardController implements Initializable {

    @FXML private VBox membersContainer;
    @FXML private Button viewAllButton;

    private final LanSessionService lanSessionService = ServiceLocator.getLanSessionService();
    private final ListChangeListener<LanMember> memberListListener = change -> renderMembers();
    
    private final List<Runnable> uiUpdateTasks = new ArrayList<>();
    private AnimationTimer loop;

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

        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                for (Runnable task : uiUpdateTasks) {
                    task.run();
                }
            }
        };
        loop.start();

        renderMembers();
    }

    private void renderMembers() {
        if (membersContainer == null) {
            return;
        }

        uiUpdateTasks.clear();
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
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("zen-team-member-row");

        Circle statusDot = new Circle(7);
        statusDot.getStyleClass().add("zen-member-dot");
        
        Label nameLabel = new Label();
        nameLabel.textProperty().bind(Bindings.createStringBinding(
                () -> member.getName() + (member.isHost() ? " (Host)" : ""),
                member.nameProperty(), member.isHostProperty()));
        nameLabel.getStyleClass().addAll("zen-member-name", "zen-team-member-name");

        Label statusLabel = new Label();
        statusLabel.getStyleClass().addAll("zen-status-label", "zen-status-pill");

        Label timerLabel = new Label();
        timerLabel.getStyleClass().add("zen-member-time");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        double radius = 18;
        Circle baseRing = new Circle(radius);
        baseRing.setFill(Color.TRANSPARENT);
        baseRing.setStroke(Color.web("#E5E7EB"));
        baseRing.setStrokeWidth(4);

        Circle progressRing = new Circle(radius);
        progressRing.setFill(Color.TRANSPARENT);
        progressRing.setStroke(Color.web("#22C55E"));
        progressRing.setStrokeWidth(4);
        progressRing.setStrokeLineCap(StrokeLineCap.ROUND);
        progressRing.getStrokeDashArray().setAll(2 * Math.PI * radius, 2 * Math.PI * radius);
        progressRing.setRotate(-90);

        Label ringLabel = new Label();
        ringLabel.getStyleClass().add("zen-ring-clock-label");

        StackPane ringClock = new StackPane(baseRing, progressRing, ringLabel);
        ringClock.getStyleClass().add("zen-ring-clock");

        uiUpdateTasks.add(() -> {
            PomodoroSyncState state = lanSessionService.getPomodoroState();
            int remainingSeconds = 25 * 60;
            boolean isRunning = false;
            String phase = "Ready";

            if (state != null) {
                isRunning = state.isRunning();
                phase = state.getPhase();
                long nowOnHostClock = System.currentTimeMillis() + lanSessionService.getHostClockOffsetMillis();
                
                if (isRunning && state.getDeadlineEpochMillis() > 0) {
                    long remainingMillis = Math.max(0, state.getDeadlineEpochMillis() - nowOnHostClock);
                    remainingSeconds = (int) Math.ceil(remainingMillis / 1000.0);
                } else {
                    long elapsedMillis = Math.max(0, nowOnHostClock - state.getCreatedAtEpochMillis());
                    int elapsedSeconds = (int) Math.floor(elapsedMillis / 1000.0);
                    remainingSeconds = Math.max(0, state.getRemainingSeconds() - elapsedSeconds);
                }
            }

            timerLabel.setText(formatTime(remainingSeconds));
            ringLabel.setText(formatTime(remainingSeconds));
            
            double fraction = Math.max(0, Math.min(1.0, remainingSeconds / (double) (25 * 60)));
            progressRing.setStrokeDashOffset((2 * Math.PI * radius) * (1.0 - fraction));
            
            statusLabel.setText(phase);
            
            statusDot.getStyleClass().removeAll("zen-status-focus-dot", "zen-status-break-dot");
            statusLabel.getStyleClass().removeAll("zen-status-focus", "zen-status-break");
            if ("Break".equalsIgnoreCase(phase)) {
                statusDot.getStyleClass().add("zen-status-break-dot");
                statusLabel.getStyleClass().add("zen-status-break");
            } else {
                statusDot.getStyleClass().add("zen-status-focus-dot");
                statusLabel.getStyleClass().add("zen-status-focus");
            }
        });

        row.getChildren().addAll(statusDot, nameLabel, spacer, timerLabel, ringClock);
        return row;
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long remaining = seconds % 60;
        return String.format("%02d:%02d", minutes, remaining);
    }
}
