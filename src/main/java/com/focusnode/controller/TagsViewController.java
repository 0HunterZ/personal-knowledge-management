package com.focusnode.controller;

import com.focusnode.repository.TagRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class TagsViewController {

    @FXML
    private Label importTagsLabel;

    private final TagRepository tagRepository = new TagRepository();

    @FXML
    private void handleImportTags(MouseEvent event) {
        Window owner = importTagsLabel.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Tags");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return;
        }

        try {
            Set<String> tags = parseTagFile(file);
            if (tags.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Import Tags", "No valid tag names were found in the selected file.");
                return;
            }

            int createdCount = tagRepository.addTags(tags, 1);
            if (createdCount > 0) {
                showAlert(Alert.AlertType.INFORMATION, "Import Tags", "Imported " + createdCount + " new tag(s) successfully.");
            } else {
                showAlert(Alert.AlertType.INFORMATION, "Import Tags", "No new tags were created. All imported tags already exist.");
            }
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Import Tags", "Failed to read the selected file: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Set<String> parseTagFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        Set<String> tagNames = new HashSet<>();
        for (String line : lines) {
            if (line == null || line.isBlank()) {
                continue;
            }
            String[] tokens = line.split("[,;\\s]+");
            for (String token : tokens) {
                String cleaned = token.trim();
                if (cleaned.isEmpty()) {
                    continue;
                }
                if (cleaned.startsWith("#")) {
                    cleaned = cleaned.substring(1).trim();
                }
                if (!cleaned.isEmpty()) {
                    tagNames.add(cleaned);
                }
            }
        }
        return tagNames;
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
