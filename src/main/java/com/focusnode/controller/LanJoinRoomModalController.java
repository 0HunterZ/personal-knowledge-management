package com.focusnode.controller;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

import java.util.concurrent.CompletableFuture;

public class LanJoinRoomModalController {

    @FXML private TextField ipInput;
    @FXML private TextField portInput;
    @FXML private PasswordField passwordInput;
    
    @FXML private Label errorLabel;
    @FXML private Button cancelBtn;
    @FXML private Button connectBtn;
    @FXML private ProgressIndicator spinner;

    private Runnable onCancel;
    private java.util.function.Consumer<com.focusnode.model.LanRoom> onSuccess;

    @FXML
    public void initialize() {
        cancelBtn.setOnAction(e -> {
            if (onCancel != null) onCancel.run();
        });

        connectBtn.setOnAction(e -> handleConnect());
    }

    public void setCallbacks(Runnable onCancel, java.util.function.Consumer<com.focusnode.model.LanRoom> onSuccess) {
        this.onCancel = onCancel;
        this.onSuccess = onSuccess;
    }

    @FXML
    public void onRecentIpClicked(MouseEvent event) {
        if (event.getSource() instanceof Label) {
            Label clickedLabel = (Label) event.getSource();
            ipInput.setText(clickedLabel.getText());
        }
    }

    private void handleConnect() {
        String ip = ipInput.getText().trim();
        if (ip.isEmpty()) {
            showError("Vui lòng nhập địa chỉ IP Host.");
            return;
        }

        int port = 5050;
        try {
            if (!portInput.getText().trim().isEmpty()) {
                port = Integer.parseInt(portInput.getText().trim());
            }
        } catch (NumberFormatException e) {
            showError("Port không hợp lệ.");
            return;
        }

        setLoading(true);
        final int targetPort = port;

        CompletableFuture.runAsync(() -> {
            try (java.net.Socket socket = new java.net.Socket()) {
                socket.connect(new java.net.InetSocketAddress(ip, targetPort), 3000); // 3 seconds timeout
                
                com.focusnode.model.LanRoom room = new com.focusnode.model.LanRoom(
                    java.util.UUID.randomUUID().toString(),
                    "Joined Room",
                    ip,
                    targetPort
                );
                
                Platform.runLater(() -> {
                    setLoading(false);
                    if (onSuccess != null) onSuccess.accept(room);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    setLoading(false);
                    showError("Không thể kết nối. Kiểm tra lại IP, Port hoặc Tường lửa (Firewall) của Host!");
                });
            }
        });
    }

    private void setLoading(boolean loading) {
        connectBtn.setDisable(loading);
        cancelBtn.setDisable(loading);
        ipInput.setDisable(loading);
        portInput.setDisable(loading);
        passwordInput.setDisable(loading);
        
        if (loading) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false);
            spinner.setVisible(true);
            spinner.setManaged(true);
            connectBtn.setText("Connecting...");
        } else {
            spinner.setVisible(false);
            spinner.setManaged(false);
            connectBtn.setText("Connect");
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
