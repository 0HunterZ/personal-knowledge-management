package com.focusnode.controller;

import com.focusnode.model.LocalProfile;
import com.focusnode.model.UserSettings;
import com.focusnode.repository.DatabaseManager;
import com.focusnode.repository.UserSettingsRepository;
import com.focusnode.service.LocalProfileManager;
import com.focusnode.service.ServiceLocator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.paint.ImagePattern;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class SettingsViewController {

    @FXML private StackPane avatarPane;
    @FXML private Circle avatarCircle;
    @FXML private Label avatarInitial;
    @FXML private Button uploadBtn;
    @FXML private Button removeBtn;
    
    @FXML private TextField displayNameField;
    @FXML private TextField emailField;
    
    // Pomodoro
    @FXML private Spinner<Integer> focusIntervalSpinner;
    @FXML private Spinner<Integer> shortBreakSpinner;
    @FXML private Spinner<Integer> longBreakSpinner;
    
    // Sync
    @FXML private ToggleButton autoSyncToggle;
    @FXML private Slider syncFrequencySlider;
    
    // Preferences
    @FXML private ToggleButton lightModeToggle;
    @FXML private ToggleButton darkModeToggle;
    @FXML private CheckBox startupCheck;
    @FXML private CheckBox soundCheck;
    @FXML private CheckBox notificationsCheck;
    @FXML private CheckBox discoveriesCheck;
    
    // Targets
    @FXML private Slider dailyTargetSlider;
    @FXML private Label dailyTargetLabel;
    @FXML private ToggleButton streak3Toggle;
    @FXML private ToggleButton streak5Toggle;
    @FXML private ToggleButton streak7Toggle;

    private LocalProfileManager profileManager;
    private UserSettingsRepository userSettingsRepository;

    @FXML
    public void initialize() {
        profileManager = ServiceLocator.getLocalProfileManager();
        userSettingsRepository = new UserSettingsRepository();
        
        // Listeners for UI updates
        dailyTargetSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            dailyTargetLabel.setText(String.format("%.1f hrs", newVal.doubleValue()));
        });
        
        loadProfileIntoView();
        loadSettingsIntoView();
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

    private void loadSettingsIntoView() {
        // Assume UserId = 1 for current user in local mode
        UserSettings settings = userSettingsRepository.findByUserId(1);
        if (settings != null) {
            if ("DARK".equalsIgnoreCase(settings.getTheme())) {
                darkModeToggle.setSelected(true);
                lightModeToggle.setSelected(false);
            } else {
                lightModeToggle.setSelected(true);
                darkModeToggle.setSelected(false);
            }
            
            double hours = settings.getDailyFocusGoalMinutes() / 60.0;
            dailyTargetSlider.setValue(hours);
            dailyTargetLabel.setText(String.format("%.1f hrs", hours));
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
        // Save local profile
        LocalProfile p = profileManager.load();
        if (p == null) p = new LocalProfile();
        p.setName(displayNameField.getText());
        profileManager.save(p);
        loadProfileIntoView();

        // Save User Settings to DB
        UserSettings settings = new UserSettings();
        settings.setUserId(1);
        settings.setTheme(darkModeToggle.isSelected() ? "DARK" : "LIGHT");
        settings.setDailyFocusGoalMinutes((int)(dailyTargetSlider.getValue() * 60));
        settings.setLanguage("VI");
        userSettingsRepository.upsert(settings);
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Settings have been saved successfully.");
        alert.setHeaderText(null);
        alert.show();
    }

    @FXML
    public void onResetSettings() {
        lightModeToggle.setSelected(true);
        darkModeToggle.setSelected(false);
        dailyTargetSlider.setValue(4.0);
        focusIntervalSpinner.getValueFactory().setValue(25);
        shortBreakSpinner.getValueFactory().setValue(5);
        longBreakSpinner.getValueFactory().setValue(15);
        startupCheck.setSelected(true);
        soundCheck.setSelected(true);
        notificationsCheck.setSelected(true);
        onSaveSettings();
    }

    @FXML
    public void onBackupDatabase() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Save Database Backup");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Server Backup", "*.bak"));
        fc.setInitialFileName("FocusNodeDB_backup.bak");
        Stage stage = (Stage) uploadBtn.getScene().getWindow();
        File destFile = fc.showSaveDialog(stage);
        
        if (destFile != null) {
            try {
                // Ensure temp dir exists
                File tempDir = new File("C:\\temp\\focusnode_backup");
                if (!tempDir.exists()) tempDir.mkdirs();
                
                String tempBackupFile = "C:\\temp\\focusnode_backup\\FocusNodeDB_export.bak";
                
                // Backup to temp
                String sql = "BACKUP DATABASE [FocusNodeDB] TO DISK = N'" + tempBackupFile + "' WITH INIT";
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.execute();
                }
                
                // Move to user destination
                Files.copy(new File(tempBackupFile).toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Database backup successful!");
                alert.setHeaderText(null);
                alert.show();
                
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Backup failed: " + e.getMessage());
                alert.show();
            }
        }
    }

    @FXML
    public void onRestoreDatabase() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Database Backup File");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SQL Server Backup", "*.bak"));
        Stage stage = (Stage) uploadBtn.getScene().getWindow();
        File sourceFile = fc.showOpenDialog(stage);
        
        if (sourceFile != null) {
            try {
                // Copy to temp directory where SQL server has access
                File tempDir = new File("C:\\temp\\focusnode_backup");
                if (!tempDir.exists()) tempDir.mkdirs();
                
                String tempRestoreFile = "C:\\temp\\focusnode_backup\\FocusNodeDB_import.bak";
                Files.copy(sourceFile.toPath(), new File(tempRestoreFile).toPath(), StandardCopyOption.REPLACE_EXISTING);
                
                String sql = "USE master; ALTER DATABASE [FocusNodeDB] SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
                             "RESTORE DATABASE [FocusNodeDB] FROM DISK = N'" + tempRestoreFile + "' WITH REPLACE; " +
                             "ALTER DATABASE [FocusNodeDB] SET MULTI_USER;";
                             
                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.execute();
                }
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Database restored successfully. Please restart the application.");
                alert.setHeaderText(null);
                alert.showAndWait();
                System.exit(0);
                
            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR, "Restore failed: " + e.getMessage());
                alert.show();
            }
        }
    }
}

