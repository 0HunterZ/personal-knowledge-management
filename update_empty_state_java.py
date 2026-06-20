import os

def write_file(path, content):
    with open(path, 'w', encoding='utf-8') as f:
        f.write(content.strip() + '\n')

# 1. LanDiscoverRoomsController.java
lan_discover_rooms = """
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
        // Empty State Default
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
        discoverRoomsContainer.setVisible(false);
        discoverRoomsContainer.setManaged(false);
        
        // Uncomment to test active state
        // renderMockRooms();
    }

    private void renderMockRooms() {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        discoverRoomsContainer.setVisible(true);
        discoverRoomsContainer.setManaged(true);
        
        discoverRoomsContainer.getChildren().clear();

        discoverRoomsContainer.getChildren().add(createRoomRow("Focus Room A", "3/6 members", "1", "#10B981", "#ECFDF5"));
        discoverRoomsContainer.getChildren().add(createRoomRow("Study Lounge", "2/6 members", "3", "#10B981", "#FFFFFF"));
        discoverRoomsContainer.getChildren().add(createRoomRow("Code Together", "4/8 members", "3", "#F59E0B", "#FFFFFF"));
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
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\java\com\focusnode\controller\LanDiscoverRoomsController.java', lan_discover_rooms)

# 2. LanActiveRoomController.java
lan_active_room = """
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
    @FXML private VBox membersContainer;
    @FXML private VBox activityContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Empty State Default
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
        activeStateContainer.setVisible(false);
        activeStateContainer.setManaged(false);
        
        // Uncomment to test active state
        // showActiveState();
    }
    
    private void showActiveState() {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        activeStateContainer.setVisible(true);
        activeStateContainer.setManaged(true);
        renderMockMembers();
        renderMockActivity();
    }

    private void renderMockMembers() {
        if (membersContainer == null) return;
        membersContainer.getChildren().clear();

        membersContainer.getChildren().add(createMemberRow("U", "You (Host) 👑", "192.168.1.200", "avatar-green", "Focusing", "status-focusing", "45:00", "🔊"));
        membersContainer.getChildren().add(createMemberRow("A", "Alice", "192.168.1.107", "avatar-purple", "Focusing", "status-focusing", "45:00", "🔇"));
        membersContainer.getChildren().add(createMemberRow("B", "Bob", "192.168.1.109", "avatar-yellow", "Break", "status-break", "10:00", "🔇"));
        membersContainer.getChildren().add(createMemberRow("C", "Charlie", "192.168.1.201", "avatar-blue", "Focusing", "status-focusing", "45:00", "🔊"));
        membersContainer.getChildren().add(createMemberRow("D", "Daniel", "192.168.1.115", "avatar-green", "Focusing", "status-focusing", "40:00", "🔊"));
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

    private void renderMockActivity() {
        if (activityContainer == null) return;
        activityContainer.getChildren().clear();

        activityContainer.getChildren().add(createActivityRow("12:00", "#10B981", "You created the room"));
        activityContainer.getChildren().add(createActivityRow("12:05", "#94A3B8", "Alice joined"));
        activityContainer.getChildren().add(createActivityRow("12:10", "#94A3B8", "Bob joined"));
        activityContainer.getChildren().add(createActivityRow("12:15", "#94A3B8", "Daniel and Eva joined"));
        activityContainer.getChildren().add(createActivityRow("12:20", "#10B981", "You started focusing"));
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
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\java\com\focusnode\controller\LanActiveRoomController.java', lan_active_room)

# 3. LanFileTransfersController.java
lan_file_transfers = """
package com.focusnode.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.ResourceBundle;

public class LanFileTransfersController implements Initializable {

    @FXML private VBox transfersContainer;
    @FXML private VBox emptyStateContainer;
    @FXML private HBox viewHistoryContainer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Empty State Default
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
        transfersContainer.setVisible(false);
        transfersContainer.setManaged(false);
        if(viewHistoryContainer != null) {
            viewHistoryContainer.setVisible(false);
            viewHistoryContainer.setManaged(false);
        }
        
        // Uncomment to test active state
        // showActiveState();
    }
    
    private void showActiveState() {
        emptyStateContainer.setVisible(false);
        emptyStateContainer.setManaged(false);
        transfersContainer.setVisible(true);
        transfersContainer.setManaged(true);
        if(viewHistoryContainer != null) {
            viewHistoryContainer.setVisible(true);
            viewHistoryContainer.setManaged(true);
        }
        renderMockTransfers();
    }

    private void renderMockTransfers() {
        if (transfersContainer == null) return;
        transfersContainer.getChildren().clear();

        transfersContainer.getChildren().add(createTransferRow("project-plan.pdf", "2.4 MB • To Alice", 1.0, "100%", "✅", "#10B981"));
        transfersContainer.getChildren().add(createTransferRow("diagram.png", "1.8 MB • To Bob", 0.72, "72%", "⏸", "#3B82F6"));
        transfersContainer.getChildren().add(createTransferRow("notes.zip", "5.8 MB • To Charlie", 0.0, "0%", "✕", "#94A3B8"));
    }

    private VBox createTransferRow(String filename, String info, double progress, String percent, String icon, String color) {
        VBox row = new VBox(8);
        row.setStyle("-fx-padding: 0 0 10 0; -fx-border-color: #F1F5F9; -fx-border-width: 0 0 1 0;");
        
        HBox top = new HBox(10);
        top.setAlignment(Pos.CENTER_LEFT);
        
        VBox texts = new VBox(2);
        Label nameLabel = new Label(filename);
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1F2937;");
        Label infoLabel = new Label(info);
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        texts.getChildren().addAll(nameLabel, infoLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: " + color + "; -fx-padding: 4; " + (icon.equals("⏸") ? "-fx-border-color: #3B82F6; -fx-border-radius: 10;" : ""));
        if(icon.equals("✅")) iconLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #10B981; -fx-border-color: #10B981; -fx-border-radius: 12; -fx-padding: 2 4;");
        
        top.getChildren().addAll(texts, spacer, iconLabel);
        
        HBox bottom = new HBox(10);
        bottom.setAlignment(Pos.CENTER_LEFT);
        
        ProgressBar pb = new ProgressBar(progress);
        pb.getStyleClass().add("transfer-progress-bar");
        pb.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(pb, Priority.ALWAYS);
        if (progress == 1.0) pb.setStyle("-fx-accent: #10B981;");
        
        Label pctLabel = new Label(percent);
        pctLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        
        bottom.getChildren().addAll(pb, pctLabel);
        
        row.getChildren().addAll(top, bottom);
        return row;
    }
}
"""
write_file(r'C:\Users\HunterZ\Desktop\FINAL EXAM JAVA\src\main\java\com\focusnode\controller\LanFileTransfersController.java', lan_file_transfers)

print("Java controllers successfully rewritten for empty state.")
