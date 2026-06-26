package com.focusnode.controller;

import com.focusnode.model.FocusSession;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import com.focusnode.util.EventBus;

public class DailyStreakController {

    @FXML private Label streakDaysLabel;
    @FXML private HBox daysContainer;

    @FXML
    public void initialize() {
        loadDataAsync();
        EventBus.subscribe(EventBus.EventType.DATA_CHANGED, e -> loadDataAsync());
    }

    private void loadDataAsync() {
        ServiceLocator.getAsyncExecutor().execute(() -> {
            List<FocusSession> sessions = ServiceLocator.getAppDataService().getFocusSessions();
            
            Set<LocalDate> activeDays = sessions.stream()
                .filter(FocusSession::isCompleted)
                .map(s -> s.getStartTime().toLocalDate())
                .collect(Collectors.toSet());

            int currentStreak = calculateStreak(activeDays);
            
            Platform.runLater(() -> {
                streakDaysLabel.setText(String.valueOf(currentStreak));
                buildDaysUI(activeDays);
            });
        });
    }

    private int calculateStreak(Set<LocalDate> activeDays) {
        int streak = 0;
        LocalDate date = LocalDate.now();
        
        if (activeDays.contains(date)) {
            streak++;
            date = date.minusDays(1);
        } else {
            if (activeDays.contains(date.minusDays(1))) {
                date = date.minusDays(1);
                streak++; // Count yesterday
                date = date.minusDays(1);
            } else {
                return 0;
            }
        }

        while (activeDays.contains(date)) {
            streak++;
            date = date.minusDays(1);
        }
        
        return streak;
    }

    private void buildDaysUI(Set<LocalDate> activeDays) {
        daysContainer.getChildren().clear();
        LocalDate startOfWeek = LocalDate.now().minusDays(6); // Last 7 days including today

        for (int i = 0; i < 7; i++) {
            LocalDate date = startOfWeek.plusDays(i);
            boolean isActive = activeDays.contains(date);
            
            StackPane dayPane = new StackPane();
            dayPane.setPrefSize(30, 30);
            
            Label dayLabel = new Label(date.getDayOfWeek().getDisplayName(TextStyle.NARROW, Locale.ENGLISH));
            dayLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            
            if (isActive) {
                dayPane.setStyle("-fx-background-color: #DCFCE7; -fx-background-radius: 15;");
                dayLabel.setStyle(dayLabel.getStyle() + "-fx-text-fill: #22C55E;");
            } else {
                dayPane.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 15;");
                dayLabel.setStyle(dayLabel.getStyle() + "-fx-text-fill: #94A3B8;");
            }
            
            dayPane.getChildren().add(dayLabel);
            daysContainer.getChildren().add(dayPane);
        }
    }
}
