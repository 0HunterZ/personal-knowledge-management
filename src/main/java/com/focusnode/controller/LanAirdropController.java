package com.focusnode.controller;

import com.focusnode.service.LanFileTransferService;
import com.focusnode.service.ServiceLocator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.VBox;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class LanAirdropController implements Initializable {

    @FXML private VBox airdropZone;

    private final LanFileTransferService transferService = ServiceLocator.getLanFileTransferService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        airdropZone.setOnDragOver(event -> {
            if (event.getGestureSource() != airdropZone && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        airdropZone.setOnDragEntered(event -> {
            if (event.getGestureSource() != airdropZone && event.getDragboard().hasFiles()) {
                airdropZone.setStyle("-fx-background-color: #DBEAFE; -fx-border-color: #3B82F6; -fx-border-width: 2; -fx-border-style: dashed; -fx-border-radius: 8; -fx-background-radius: 8;");
            }
            event.consume();
        });

        airdropZone.setOnDragExited(event -> {
            airdropZone.setStyle("");
            airdropZone.getStyleClass().add("airdrop-zone"); // Reset to CSS class
            event.consume();
        });

        airdropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                success = true;
                for (File file : db.getFiles()) {
                    transferService.sendFile(file, "Everyone"); // Broadcast or prompt for target
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
