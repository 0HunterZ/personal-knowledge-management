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
        showEmptyState();
        
        com.focusnode.service.ServiceLocator.getLanSessionService().activeRoomProperty().addListener((obs, oldV, newV) -> {
            javafx.application.Platform.runLater(() -> {
                if (newV != null) {
                    showActiveState();
                } else {
                    showEmptyState();
                }
            });
        });
        
        if (viewHistoryContainer != null) {
            viewHistoryContainer.setOnMouseClicked(e -> showTransferHistory());
        }
    }
    
    private void showTransferHistory() {
        try {
            com.focusnode.repository.LanTransferHistoryRepository repo = new com.focusnode.repository.LanTransferHistoryRepository();
            java.util.List<com.focusnode.model.LanTransferHistory> history = repo.getHistoryByUserId(1); // Default user 1
            
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setTitle("File Transfer History");
            alert.setHeaderText("Recent LAN File Transfers");
            
            if (history.isEmpty()) {
                alert.setContentText("No transfer history found.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (com.focusnode.model.LanTransferHistory h : history) {
                    sb.append(String.format("[%s] %s (%.1f MB) - %s\nTarget: %s\n\n",
                        h.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
                        h.getFileName(),
                        h.getSizeBytes() / 1024.0 / 1024.0,
                        h.getStatus(),
                        h.getTargetName()
                    ));
                }
                
                javafx.scene.control.TextArea area = new javafx.scene.control.TextArea(sb.toString());
                area.setEditable(false);
                area.setWrapText(true);
                area.setMaxWidth(Double.MAX_VALUE);
                area.setMaxHeight(Double.MAX_VALUE);
                
                javafx.scene.layout.GridPane expContent = new javafx.scene.layout.GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(area, 0, 0);
                
                alert.getDialogPane().setExpandableContent(expContent);
                alert.getDialogPane().setExpanded(true);
            }
            alert.showAndWait();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showEmptyState() {
        emptyStateContainer.setVisible(true);
        emptyStateContainer.setManaged(true);
        transfersContainer.setVisible(false);
        transfersContainer.setManaged(false);
        if(viewHistoryContainer != null) {
            viewHistoryContainer.setVisible(false);
            viewHistoryContainer.setManaged(false);
        }
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
        
        // Listen for new transfers
        com.focusnode.service.ServiceLocator.getLanSessionService().getActiveTransfers().addListener(
            (javafx.collections.ListChangeListener.Change<? extends com.focusnode.model.LanTransfer> c) -> {
                javafx.application.Platform.runLater(() -> renderMockTransfers());
            }
        );
        renderMockTransfers();
    }

    private void renderMockTransfers() {
        if (transfersContainer == null) return;
        transfersContainer.getChildren().clear();

        for (com.focusnode.model.LanTransfer t : com.focusnode.service.ServiceLocator.getLanSessionService().getActiveTransfers()) {
            String icon = t.getStatus().equals("COMPLETED") ? "✅" : (t.getStatus().equals("FAILED") ? "✕" : "⏸");
            String color = t.getStatus().equals("COMPLETED") ? "#10B981" : (t.getStatus().equals("FAILED") ? "#ef4444" : "#3B82F6");
            
            // Re-render row whenever progress/status changes
            VBox row = createTransferRow(t.getFilename(), 
                    String.format("%.1f MB • To %s", t.getTotalBytes() / 1024.0 / 1024.0, t.getTargetName()), 
                    t.getProgress(), 
                    (int)(t.getProgress() * 100) + "%", 
                    icon, color);
            
            t.progressProperty().addListener((obs, oldVal, newVal) -> {
                javafx.application.Platform.runLater(() -> renderMockTransfers());
            });
            t.statusProperty().addListener((obs, oldVal, newVal) -> {
                javafx.application.Platform.runLater(() -> renderMockTransfers());
            });
            
            transfersContainer.getChildren().add(row);
        }
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
