package com.focusnode.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

public class LanCreateRoomModalController {

    @FXML private TextField roomNameInput;
    @FXML private TextField portInput;
    @FXML private CheckBox requirePasswordToggle;
    @FXML private VBox passwordContainer;
    @FXML private PasswordField passwordInput;
    
    @FXML private Label errorLabel;
    @FXML private Button cancelBtn;
    @FXML private Button startHostingBtn;
    @FXML private ProgressIndicator spinner;

    private Runnable onCancel;
    private java.util.function.Consumer<com.focusnode.model.LanRoom> onSuccess;

    @FXML
    public void initialize() {
        requirePasswordToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            passwordContainer.setVisible(newVal);
            passwordContainer.setManaged(newVal);
        });

        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });

        startHostingBtn.setOnAction(e -> handleStartHosting());
    }

    public void setCallbacks(Runnable onCancel, java.util.function.Consumer<com.focusnode.model.LanRoom> onSuccess) {
        this.onCancel = onCancel;
        this.onSuccess = onSuccess;
    }

    private void handleStartHosting() {
        // Validation
        if (roomNameInput.getText().trim().isEmpty()) {
            showError("Vui lòng nhập tên phòng.");
            return;
        }

        // Show loading state
        setLoading(true);

        CompletableFuture.runAsync(() -> {
            try {
                // Get real local IP address
                String localIp = java.net.InetAddress.getLocalHost().getHostAddress();
                int port = Integer.parseInt(portInput.getText().isEmpty() ? "5050" : portInput.getText());
                
                com.focusnode.model.LanRoom room = new com.focusnode.model.LanRoom(
                    java.util.UUID.randomUUID().toString(), 
                    roomNameInput.getText().trim(), 
                    localIp, 
                    port
                );
                
                Platform.runLater(() -> {
                    setLoading(false);
                    if (onSuccess != null) onSuccess.accept(room);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Không thể lấy địa chỉ IP LAN.");
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        startHostingBtn.setDisable(loading);
        cancelBtn.setDisable(loading);
        roomNameInput.setDisable(loading);
        portInput.setDisable(loading);
        requirePasswordToggle.setDisable(loading);
        passwordInput.setDisable(loading);
        
        if (loading) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            spinner.setVisible(true);
            spinner.setManaged(true);
            startHostingBtn.setText("Connecting...");
        } else {
            spinner.setVisible(false);
            spinner.setManaged(false);
            startHostingBtn.setText("Start Hosting");
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);

        TranslateTransition shake = new TranslateTransition(Duration.millis(50), errorLabel);
        shake.setByX(10f);
        shake.setCycleCount(6);
        shake.setAutoReverse(true);
        shake.playFromStart();
    }
}
