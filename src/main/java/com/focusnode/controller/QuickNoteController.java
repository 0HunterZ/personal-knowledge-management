package com.focusnode.controller;

import com.focusnode.model.Note;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.time.LocalDate;
import java.util.List;

public class QuickNoteController {
    
    @FXML private TextArea noteArea;
    @FXML private Label charCountLabel;
    
    private Runnable onNoteSaved;

    public void setOnNoteSaved(Runnable onNoteSaved) {
        this.onNoteSaved = onNoteSaved;
    }

    @FXML
    public void initialize() {
        noteArea.textProperty().addListener((obs, oldVal, newVal) -> {
            charCountLabel.setText(newVal.length() + " / 200");
            if (newVal.length() > 200) {
                noteArea.setText(oldVal);
            }
        });
    }

    @FXML
    private void saveQuickNote() {
        String content = noteArea.getText().trim();
        if (content.isEmpty()) return;

        ServiceLocator.getAsyncExecutor().execute(() -> {
            Note newNote = new Note(
                -1,
                1,
                null,
                null,
                "Quick Note",
                content,
                null,
                List.of("inbox"),
                java.time.LocalDateTime.now(),
                java.time.LocalDateTime.now(),
                false
            );
            
            // Save to database
            ServiceLocator.getAppDataService().saveNote(newNote);
            
            Platform.runLater(() -> {
                noteArea.clear();
                noteArea.setPromptText("Saved successfully! Write another...");
                if (onNoteSaved != null) {
                    onNoteSaved.run();
                }
                ServiceLocator.getAsyncExecutor().execute(() -> {
                    try { Thread.sleep(2000); } catch (Exception e) {}
                    Platform.runLater(() -> noteArea.setPromptText("Write something...\n(press Enter to save)"));
                });
            });
        });
    }
}
