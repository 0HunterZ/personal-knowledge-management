package com.focusnode.controller;

import com.focusnode.model.LocalProfile;
import com.focusnode.service.LocalProfileManager;
import com.focusnode.service.ServiceLocator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class SettingsViewController {

    @FXML
    private StackPane avatarPane;

    @FXML
    private Circle avatarCircle;

    @FXML
    private Label avatarInitial;

    @FXML
    private Button uploadBtn;

    @FXML
    private Button removeBtn;

    @FXML
    private TextField displayNameField;

    @FXML
    private TextField emailField;

    private LocalProfileManager profileManager;

    @FXML
    public void initialize() {
        profileManager = ServiceLocator.getLocalProfileManager();
        loadProfileIntoView();
    }

    private void loadProfileIntoView() {
        LocalProfile p = profileManager.load();
        if (p != null) {
            displayNameField.setText(p.getName() == null ? "" : p.getName());
            if (p.getAvatarPath() != null && !p.getAvatarPath().isBlank()) {
                try {
                    Image img = new Image(new File(p.getAvatarPath()).toURI().toString());
                    avatarCircle.setFill(new ImagePattern(img));
                    avatarInitial.setVisible(false);
                } catch (Exception e) {
                    avatarCircle.setFill(javafx.scene.paint.Paint.valueOf("#8B5CF6"));
                    avatarInitial.setText(p.getName() != null && !p.getName().isBlank() ? p.getName().substring(0,1).toUpperCase() : "U");
                    avatarInitial.setVisible(true);
                }
            } else {
                avatarInitial.setText(p.getName() != null && !p.getName().isBlank() ? p.getName().substring(0,1).toUpperCase() : "U");
                avatarInitial.setVisible(true);
            }
        }
    }

    @FXML
    public void onUploadAvatar() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        Stage stage = (Stage) uploadBtn.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null && file.exists()) {
            LocalProfile p = profileManager.load();
            if (p == null) p = new LocalProfile(displayNameField.getText(), null);
            profileManager.save(p, file);
            loadProfileIntoView();
        }
    }

    @FXML
    public void onRemoveAvatar() {
        LocalProfile p = profileManager.load();
        if (p == null) return;
        p.setAvatarPath(null);
        profileManager.save(p);
        loadProfileIntoView();
    }

    @FXML
    public void onEditProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dialogs/LocalProfileDialog.fxml"));
            Stage dlg = new Stage();
            dlg.initOwner(displayNameField.getScene().getWindow());
            dlg.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dlg.setScene(new javafx.scene.Scene(loader.load()));
            dlg.setTitle("Edit Local Profile");
            dlg.showAndWait();
            loadProfileIntoView();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onSaveSettings() {
        // Save current display name into the local profile for local-first behavior
        LocalProfile p = profileManager.load();
        if (p == null) p = new LocalProfile();
        p.setName(displayNameField.getText());
        profileManager.save(p);
        loadProfileIntoView();
    }
}

