package com.focusnode.controller;

import com.focusnode.model.Task;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import com.focusnode.navigation.AppNavigator;
import com.focusnode.navigation.AppView;
import com.focusnode.util.EventBus;
import com.focusnode.navigation.AppNavigator;
import com.focusnode.navigation.AppView;

public class TodaysFocusController {

    @FXML private VBox taskListContainer;

    @FXML
    public void initialize() {
        loadTasksAsync();
        EventBus.subscribe(EventBus.EventType.DATA_CHANGED, e -> loadTasksAsync());
    }

    private void loadTasksAsync() {
        ServiceLocator.getAsyncExecutor().execute(() -> {
            List<Task> allTasks = ServiceLocator.getAppDataService().getTasks();
            LocalDate today = LocalDate.now();
            
            // Get incomplete tasks due today or overdue
            List<Task> focusTasks = allTasks.stream()
                .filter(t -> t.getStatus() != Task.Status.COMPLETED)
                .filter(t -> t.getDueDate() != null && !t.getDueDate().toLocalDate().isAfter(today))
                .limit(4)
                .collect(Collectors.toList());

            Platform.runLater(() -> {
                taskListContainer.getChildren().clear();
                if (focusTasks.isEmpty()) {
                    Label emptyLabel = new Label("No tasks for today. You're all caught up!");
                    emptyLabel.setStyle("-fx-text-fill: #94A3B8;");
                    taskListContainer.getChildren().add(emptyLabel);
                    return;
                }

                for (Task task : focusTasks) {
                    HBox row = new HBox(10);
                    row.setStyle("-fx-alignment: center-left; -fx-padding: 8 0; -fx-cursor: hand;");
                    
                    Circle dot = new Circle(6);
                    dot.setFill(javafx.scene.paint.Color.TRANSPARENT);
                    dot.setStroke(javafx.scene.paint.Color.web("#22C55E"));
                    dot.setStrokeWidth(2);

                    Label title = new Label(task.getTitle());
                    title.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 13px; -fx-font-weight: bold;");

                    Label category = new Label(task.getCategory());
                    // Pick color based on category
                    String catBg = "#E0F2FE";
                    String catText = "#0284C7";
                    if (task.getCategory().equalsIgnoreCase("Work")) {
                        catBg = "#F3E8FF"; catText = "#9333EA";
                    } else if (task.getCategory().equalsIgnoreCase("Study")) {
                        catBg = "#DCFCE7"; catText = "#16A34A";
                    } else if (task.getCategory().equalsIgnoreCase("Personal")) {
                        catBg = "#FEF3C7"; catText = "#D97706";
                    }
                    category.setStyle("-fx-background-color: " + catBg + "; -fx-text-fill: " + catText + "; -fx-padding: 3 8; -fx-background-radius: 9999px; -fx-font-size: 11px; -fx-font-weight: 600;");

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Label time = new Label(task.getFocusMinutes() + " min");
                    time.setStyle("-fx-background-color: #F8FAFC; -fx-padding: 4 10; -fx-background-radius: 12; -fx-text-fill: #6B7280; -fx-font-size: 12px; -fx-font-weight: 600;");

                    row.getChildren().addAll(dot, title, category, spacer, time);
                    
                    row.setOnMouseEntered(e -> title.setStyle("-fx-text-fill: #22C55E; -fx-font-size: 13px; -fx-font-weight: bold;"));
                    row.setOnMouseExited(e -> title.setStyle("-fx-text-fill: #1F2937; -fx-font-size: 13px; -fx-font-weight: bold;"));
                    
                    taskListContainer.getChildren().add(row);
                }
            });
        });
    }

    @FXML
    private void addTask() {
        AppNavigator.navigateTo(AppView.TASKS);
    }
}
