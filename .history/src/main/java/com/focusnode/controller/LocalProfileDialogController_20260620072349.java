package com.focusnode.controller;

import com.focusnode.model.LocalProfile;
import com.focusnode.service.ServiceLocator;
import com.focusnode.service.LocalProfileManager;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class LocalProfileDialogController {
    @FXML
    private TextField nameField;

    @FXML
    private ImageView avatarView;

    private File selectedAvatar;

    private LocalProfileManager profileManager;

    @FXML
    public void initialize() {
        profileManager = ServiceLocator.getLocalProfileManager();
        LocalProfile existing = profileManager.load();
        if (existing != null) {
            nameField.setText(existing.getName());
            if (existing.getAvatarPath() != null && !existing.getAvatarPath().isBlank()) {
                try {
                    avatarView.setImage(new Image(new File(existing.getAvatarPath()).toURI().toString()));
                } catch (Exception ignored) {
                }
            }
        }
    }

    @FXML
    public void onChooseAvatar() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );
        Stage stage = (Stage) nameField.getScene().getWindow();
        File file = fc.showOpenDialog(stage);
        if (file != null && file.exists()) {
            selectedAvatar = file;
            avatarView.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    public void onSave() {
        String name = nameField.getText();
        if (name == null || name.isBlank()) {
            // simple validation
            nameField.requestFocus();
            return;
        }

        String avatarPath = selectedAvatar == null ? null : selectedAvatar.getAbsolutePath();
        LocalProfile profile = new LocalProfile(name.trim(), avatarPath);
        profileManager.save(profile);

        // close dialog
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }
}
