package com.focusnode.controller;

import com.focusnode.model.Note;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;
import com.focusnode.navigation.AppNavigator;
import com.focusnode.navigation.AppView;
import com.focusnode.util.EventBus;

public class RecentNotesController {

    @FXML private HBox notesContainer;

    @FXML
    public void initialize() {
        loadRecentNotes();
        EventBus.subscribe(EventBus.EventType.DATA_CHANGED, e -> loadRecentNotes());
    }

    public void loadRecentNotes() {
        ServiceLocator.getAsyncExecutor().execute(() -> {
            List<Note> allNotes = ServiceLocator.getAppDataService().getNotes();
            
            String[] fixedTags = {"Project", "Study", "Idea", "Review"};
            List<Note> displayNotes = new ArrayList<>();

            for (String targetTag : fixedTags) {
                Optional<Note> foundNote = allNotes.stream()
                    .filter(n -> n.getTags() != null && n.getTags().stream().anyMatch(t -> t.equalsIgnoreCase(targetTag)))
                    .max(Comparator.comparing(Note::getUpdatedAt));

                if (foundNote.isPresent()) {
                    displayNotes.add(foundNote.get());
                } else {
                    Note dummyNote = new Note(-1, 1, null, null, "No note for " + targetTag, "", null, java.util.Arrays.asList(targetTag), java.time.LocalDateTime.now(), java.time.LocalDateTime.now(), false);
                    displayNotes.add(dummyNote);
                }
            }

            Platform.runLater(() -> {
                notesContainer.getChildren().clear();

                for (Note note : displayNotes) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/NoteCard.fxml"));
                        VBox noteCard = loader.load();
                        NoteCardController controller = loader.getController();
                        controller.setNote(note);
                        notesContainer.getChildren().add(noteCard);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
    }

    @FXML
    private void viewAllNotes() {
        AppNavigator.navigateTo(AppView.KNOWLEDGE);
    }
}
