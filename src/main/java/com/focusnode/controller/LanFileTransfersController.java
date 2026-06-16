package com.focusnode.controller;

import com.focusnode.model.LanTransfer;
import com.focusnode.service.LanSessionService;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
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

    private final LanSessionService sessionService = ServiceLocator.getLanSessionService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        renderTransfers();

        sessionService.getActiveTransfers().addListener((ListChangeListener<LanTransfer>) change -> {
            Platform.runLater(this::renderTransfers);
        });
    }

    private void renderTransfers() {
        transfersContainer.getChildren().clear();

        if (sessionService.getActiveTransfers().isEmpty()) {
            Label empty = new Label("No active transfers.");
            empty.setStyle("-fx-text-fill: #94A3B8; -fx-padding: 10;");
            transfersContainer.getChildren().add(empty);
            return;
        }

        for (LanTransfer transfer : sessionService.getActiveTransfers()) {
            transfersContainer.getChildren().add(createTransferRow(transfer));
        }
    }

    private VBox createTransferRow(LanTransfer transfer) {
        VBox row = new VBox(8);
        row.setStyle("-fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-padding: 10; -fx-background-color: white;");
        
        // Hover spell
        row.setOnMouseEntered(e -> row.setStyle("-fx-border-color: #CBD5E1; -fx-border-radius: 8; -fx-padding: 10; -fx-background-color: #F8FAFC; -fx-cursor: hand;"));
        row.setOnMouseExited(e -> row.setStyle("-fx-border-color: #E2E8F0; -fx-border-radius: 8; -fx-padding: 10; -fx-background-color: white;"));

        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox info = new VBox();
        Label nameLabel = new Label();
        nameLabel.textProperty().bind(transfer.filenameProperty());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1F2937;");
        
        Label metaLabel = new Label();
        metaLabel.textProperty().bind(transfer.totalBytesProperty().map(bytes -> {
            double mb = bytes.longValue() / 1048576.0;
            return String.format("%.1f MB • To %s", mb, transfer.getTargetName());
        }));
        metaLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");
        info.getChildren().addAll(nameLabel, metaLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action/Status Icon
        Label statusIcon = new Label();
        statusIcon.textProperty().bind(transfer.statusProperty().map(status -> switch (status) {
            case "COMPLETED" -> "✓";
            case "TRANSFERRING" -> "⏸";
            default -> "✕";
        }));
        
        // Style bindings for status icon
        transfer.statusProperty().addListener((obs, oldV, newV) -> {
            if ("COMPLETED".equals(newV)) {
                statusIcon.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 16px;");
                statusIcon.getStyleClass().clear();
            } else {
                statusIcon.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 4; -fx-text-fill: #64748B;");
                statusIcon.getStyleClass().add("btn-icon-transparent");
            }
        });
        
        // Initial setup for status icon style
        if ("COMPLETED".equals(transfer.getStatus())) {
            statusIcon.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 16px;");
        } else {
            statusIcon.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 4; -fx-text-fill: #64748B;");
            statusIcon.getStyleClass().add("btn-icon-transparent");
        }

        topRow.getChildren().addAll(info, spacer, statusIcon);

        HBox bottomRow = new HBox(10);
        bottomRow.setAlignment(Pos.CENTER_LEFT);

        ProgressBar progressBar = new ProgressBar();
        progressBar.progressProperty().bind(transfer.progressProperty());
        progressBar.setPrefWidth(180);
        progressBar.getStyleClass().add("transfer-progress-bar");
        
        // Dynamic accent color based on status
        transfer.statusProperty().addListener((obs, oldV, newV) -> {
            if ("COMPLETED".equals(newV)) {
                progressBar.setStyle("-fx-accent: #22C55E;");
            } else if ("TRANSFERRING".equals(newV)) {
                progressBar.setStyle("-fx-accent: #3B82F6;");
            } else {
                progressBar.setStyle("-fx-accent: #E2E8F0;");
            }
        });
        
        // Initial progress color
        if ("COMPLETED".equals(transfer.getStatus())) {
            progressBar.setStyle("-fx-accent: #22C55E;");
        } else if ("TRANSFERRING".equals(transfer.getStatus())) {
            progressBar.setStyle("-fx-accent: #3B82F6;");
        } else {
            progressBar.setStyle("-fx-accent: #E2E8F0;");
        }

        Label pctLabel = new Label();
        pctLabel.textProperty().bind(transfer.progressProperty().map(prog -> 
            String.format("%.0f%%", prog.doubleValue() * 100)
        ));
        pctLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #6B7280;");

        bottomRow.getChildren().addAll(progressBar, pctLabel);

        row.getChildren().addAll(topRow, bottomRow);
        return row;
    }
}
