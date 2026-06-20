package com.focusnode.controller;

import com.focusnode.model.LanRoom;
import com.focusnode.model.LanMember;
import com.focusnode.service.LanSessionService;
import com.focusnode.service.ServiceLocator;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class LanHubViewController {

    @FXML private LanRoomActionsController lanRoomActionsController;
    @FXML private LanCreateRoomModalController createRoomModalController;
    @FXML private LanJoinRoomModalController joinRoomModalController;

    @FXML private Region modalOverlay;
    @FXML private StackPane modalsContainer;
    @FXML private VBox createRoomModal;
    @FXML private VBox joinRoomModal;
    @FXML private VBox actionsColumn;
    @FXML private VBox fileTransfersColumn;

    private final LanSessionService sessionService = ServiceLocator.getLanSessionService();

    @FXML
    public void initialize() {
        ServiceLocator.getLanDiscoveryService().startListening();

        // Setup Room Actions callbacks
        if (lanRoomActionsController != null) {
            lanRoomActionsController.setCallbacks(
                () -> showModal(createRoomModal),
                () -> showModal(joinRoomModal)
            );
        }

        // Setup Modals callbacks
        if (createRoomModalController != null) {
            createRoomModalController.setCallbacks(
                this::hideModals,
                (com.focusnode.model.LanRoom room) -> {
                    hideModals();
                    // Real host room logic
                    LanMember host = new LanMember(UUID.randomUUID().toString(), "Host", room.getHostIp(), true);
                    room.getMembers().add(host);
                    sessionService.hostRoom(room);
                    ServiceLocator.getLanDiscoveryService().startBroadcasting(room);
                }
            );
        }

        if (joinRoomModalController != null) {
            joinRoomModalController.setCallbacks(
                this::hideModals,
                (com.focusnode.model.LanRoom room) -> {
                    hideModals();
                    // Real join room logic
                    LanMember me = new LanMember(UUID.randomUUID().toString(), "You", "127.0.0.1", false);
                    sessionService.joinRoom(room, me);
                }
            );
        }

        // Listen for active room changes to toggle UI columns
        sessionService.activeRoomProperty().addListener((obs, oldVal, newVal) -> {
            boolean inRoom = newVal != null;
            if (actionsColumn != null) {
                actionsColumn.setVisible(!inRoom);
                actionsColumn.setManaged(!inRoom);
            }
            if (fileTransfersColumn != null) {
                fileTransfersColumn.setVisible(inRoom);
                fileTransfersColumn.setManaged(inRoom);
            }
        });
    }

    private void showModal(VBox modal) {
        modalOverlay.setVisible(true);
        modalOverlay.setManaged(true);
        
        modalsContainer.setVisible(true);
        modalsContainer.setManaged(true);
        
        createRoomModal.setVisible(modal == createRoomModal);
        createRoomModal.setManaged(modal == createRoomModal);
        
        joinRoomModal.setVisible(modal == joinRoomModal);
        joinRoomModal.setManaged(modal == joinRoomModal);
    }

    private void hideModals() {
        modalOverlay.setVisible(false);
        modalOverlay.setManaged(false);
        
        modalsContainer.setVisible(false);
        modalsContainer.setManaged(false);
        
        createRoomModal.setVisible(false);
        createRoomModal.setManaged(false);
        
        joinRoomModal.setVisible(false);
        joinRoomModal.setManaged(false);
    }

    // Export/Import database functionality
    @FXML
    public void onExportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Database");
        fileChooser.setInitialFileName("focusnode_backup.db");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite DB", "*.db"));
        
        Window window = javafx.stage.Window.getWindows().stream().filter(Window::isFocused).findFirst().orElse(null);
        if (window == null && !javafx.stage.Window.getWindows().isEmpty()) {
            window = javafx.stage.Window.getWindows().get(0);
        }
        
        File file = fileChooser.showSaveDialog(window);
        
        if (file != null) {
            try {
                File dbFile = new File("focusnode.db");
                if (dbFile.exists()) {
                    Files.copy(dbFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    showAlert("Export Successful", "Data exported to " + file.getAbsolutePath());
                } else {
                    showAlert("Export Failed", "Database file not found locally.");
                }
            } catch (Exception e) {
                showAlert("Export Error", e.getMessage());
            }
        }
    }

    @FXML
    public void onImportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Database");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQLite DB", "*.db"));
        
        Window window = javafx.stage.Window.getWindows().stream().filter(Window::isFocused).findFirst().orElse(null);
        if (window == null && !javafx.stage.Window.getWindows().isEmpty()) {
            window = javafx.stage.Window.getWindows().get(0);
        }
        
        File file = fileChooser.showOpenDialog(window);
        
        if (file != null) {
            try {
                File dbFile = new File("focusnode.db");
                Files.copy(file.toPath(), dbFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                showAlert("Import Successful", "Data imported successfully. Please restart the app to see changes.");
            } catch (Exception e) {
                showAlert("Import Error", e.getMessage());
            }
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
