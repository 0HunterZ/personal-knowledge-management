package com.focusnode.controller;

import com.focusnode.model.Note;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.shape.SVGPath;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;

public class NoteCardController {
    
    @FXML private Label titleLabel;
    @FXML private Label previewLabel;
    @FXML private Region iconBackground;
    @FXML private SVGPath iconSvg;
    @FXML private HBox tagsContainer;
    @FXML private Label updatedAtLabel;
    
    public void setNote(Note note) {
        boolean isEmptyNote = (note.getTitle() == null || note.getTitle().isEmpty()) && (note.getContent() == null || note.getContent().isEmpty());
        
        String displayTitle = "Untitled Note";
        if (note.getTitle() != null && !note.getTitle().isEmpty()) {
            displayTitle = note.getTitle();
        } else if (isEmptyNote && note.getTags() != null && !note.getTags().isEmpty()) {
            displayTitle = note.getTags().get(0) + " Note"; // E.g., "Project Note"
        }
        
        titleLabel.setText(displayTitle);
        
        if (isEmptyNote) {
            previewLabel.setText("No recent notes found. Click to create one.");
            previewLabel.setStyle("-fx-text-fill: #94A3B8; -fx-font-style: italic;");
        } else {
            previewLabel.setText(note.getContent() != null ? note.getContent() : "");
            previewLabel.setStyle("-fx-text-fill: #6B7280;");
        }

        // Dynamic icon and background color based on title or tags
        String svgContent = "M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z"; // Default Note
        String svgColor = "#9333EA"; // Purple
        String bgColor = "#F3E8FF"; // Purple light

        String textToMatch = note.getTitle() != null ? note.getTitle().toLowerCase() : "";
        if (note.getTags() != null && !note.getTags().isEmpty()) {
            textToMatch += " " + note.getTags().get(0).toLowerCase();
        }
        
        if (textToMatch.contains("project") || textToMatch.contains("plan")) {
            svgContent = "M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"; // Task
            svgColor = "#0284C7"; // Blue
            bgColor = "#E0F2FE"; // Blue light
        } else if (textToMatch.contains("study") || textToMatch.contains("java") || textToMatch.contains("code")) {
            svgContent = "M12 3L1 9l4 2.18v6L12 21l7-3.82v-6l2-1.09V17h2V9L12 3zm6.82 6L12 12.72 5.18 9 12 5.28 18.82 9zM17 15.99l-5 2.73-5-2.73v-3.72l5 2.73 5-2.73v3.72z"; // Knowledge
            svgColor = "#16A34A"; // Green
            bgColor = "#DCFCE7"; // Green light
        } else if (textToMatch.contains("idea") || textToMatch.contains("tip") || textToMatch.contains("mind")) {
            svgContent = "M12.586 2.586A2 2 0 0011.172 2H4a2 2 0 00-2 2v7.172a2 2 0 00.586 1.414l8 8a2 2 0 002.828 0l7.172-7.172a2 2 0 000-2.828l-8-8zM7 9a2 2 0 110-4 2 2 0 010 4z"; // Tag
            svgColor = "#D97706"; // Orange
            bgColor = "#FEF3C7"; // Orange light
        } else if (textToMatch.contains("review") || textToMatch.contains("ai")) {
            svgContent = "M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z"; // Focus
            svgColor = "#9333EA"; // Purple
            bgColor = "#F3E8FF"; // Purple light
        }
        
        if (iconBackground != null) {
            iconBackground.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-pref-width: 44; -fx-pref-height: 44;");
        }
        
        if (iconSvg != null) {
            iconSvg.setContent(svgContent);
            iconSvg.setStyle("-fx-fill: " + svgColor + ";");
        }

        // Tags
        if (tagsContainer != null) {
            tagsContainer.getChildren().clear();
            if (note.getTags() != null) {
                for (String tag : note.getTags()) {
                    Label tagLabel = new Label("#" + tag);
                    tagLabel.getStyleClass().add("tag-badge");
                    if (tag.equalsIgnoreCase("project") || tag.equalsIgnoreCase("plan")) {
                        tagLabel.setStyle("-fx-background-color: #E0F2FE; -fx-text-fill: #0284C7;");
                    } else if (tag.equalsIgnoreCase("study") || tag.equalsIgnoreCase("java") || tag.equalsIgnoreCase("code")) {
                        tagLabel.setStyle("-fx-background-color: #DCFCE7; -fx-text-fill: #16A34A;");
                    } else if (tag.equalsIgnoreCase("review") || tag.equalsIgnoreCase("AI")) {
                        tagLabel.setStyle("-fx-background-color: #F3E8FF; -fx-text-fill: #9333EA;");
                    } else if (tag.equalsIgnoreCase("idea")) {
                        tagLabel.setStyle("-fx-background-color: #FEF3C7; -fx-text-fill: #D97706;");
                    } else {
                        tagLabel.setStyle("-fx-background-color: #EEF2FF; -fx-text-fill: #4F46E5;");
                    }
                    tagsContainer.getChildren().add(tagLabel);
                }
            }
        }

        // Updated at
        if (updatedAtLabel != null) {
            if (isEmptyNote) {
                updatedAtLabel.setText("Not started");
            } else if (note.getUpdatedAt() != null) {
                long days = ChronoUnit.DAYS.between(note.getUpdatedAt().toLocalDate(), LocalDate.now());
                if (days == 0) {
                    updatedAtLabel.setText("Updated today");
                } else if (days == 1) {
                    updatedAtLabel.setText("Updated yesterday");
                } else {
                    updatedAtLabel.setText("Updated " + days + " days ago");
                }
            }
        }
    }
}
