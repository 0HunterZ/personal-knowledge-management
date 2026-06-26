package com.focusnode.controller;

import com.focusnode.model.Task;
import com.focusnode.service.AppDataService;
import com.focusnode.service.ServiceLocator;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import com.focusnode.util.EventBus;

public class TasksViewController {

    @FXML private TextField searchField;
    @FXML private Label taskSummaryLabel;
    
    @FXML private Label completedCountLabel;
    @FXML private Label inProgressCountLabel;
    @FXML private Label pendingCountLabel;
    @FXML private Label totalCountLabel;

    @FXML private VBox taskListContainer;
    @FXML private ScrollPane mainScrollPane;
    @FXML private StackPane editorContainer;
    @FXML private VBox rightSidebar;
    
    @FXML private Label filterAll;
    @FXML private Label filterToday;
    @FXML private Label filterUpcoming;
    @FXML private Label filterCompleted;

    @FXML private Label paginationSummaryLabel;
    @FXML private HBox paginationContainer;

    private TaskEditorController taskEditorController;
    private Node taskEditorNode;

    private final AppDataService dataService = ServiceLocator.getAppDataService();
    
    private String currentFilterType = "All";
    private int currentPage = 1;
    private int itemsPerPage = 8;
    private List<Task> allTasks = new java.util.ArrayList<>();

    @FXML
    public void initialize() {
        if (searchField != null) {
            searchField.textProperty().addListener((observable, oldValue, newValue) -> filterTaskList(newValue));
        }

        loadEditor();
        loadTasksAsync("");
        EventBus.subscribe(EventBus.EventType.DATA_CHANGED, e -> 
            loadTasksAsync(searchField != null ? searchField.getText() : "")
        );
    }

    private void loadEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/TaskEditor.fxml"));
            taskEditorNode = loader.load();
            taskEditorController = loader.getController();
            if (editorContainer != null) {
                editorContainer.getChildren().add(taskEditorNode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void createNewTask() {
        openEditor(null);
    }

    private void openEditor(Task task) {
        if (taskEditorController != null && mainScrollPane != null) {
            taskEditorController.setTask(task, () -> {
                closeEditor();
                loadTasksAsync(searchField != null ? searchField.getText() : "");
            }, this::closeEditor);
            editorContainer.setVisible(true);
        }
    }

    private void closeEditor() {
        if (editorContainer != null) {
            editorContainer.setVisible(false);
        }
    }

    private void loadTasksAsync(String query) {
        com.focusnode.util.AsyncExecutor.execute(() -> {
            List<Task> tasks = dataService.getTasks();
            javafx.application.Platform.runLater(() -> {
                this.allTasks = tasks;
                filterTaskList(query);
            });
        });
    }

    private void filterTaskList(String query) {
        if (taskListContainer == null) {
            return;
        }

        List<Task> filtered = allTasks.stream()
                .filter(task -> matchesQuery(task, query))
                .sorted((t1, t2) -> {
                    int pCmp = t1.getPriority().compareTo(t2.getPriority());
                    if (pCmp != 0) return pCmp;
                    int dCmp = t1.getDueDate().compareTo(t2.getDueDate());
                    if (dCmp != 0) return dCmp;
                    return Integer.compare(t2.getFocusMinutes(), t1.getFocusMinutes());
                })
                .collect(Collectors.toList());

        updateStats(filtered);

        int totalItems = filtered.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int startIndex = (currentPage - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, totalItems);

        List<Task> pageItems = filtered.subList(startIndex, endIndex);

        taskListContainer.getChildren().clear();
        for (Task task : pageItems) {
            Node row = loadTaskRow(task);
            if (row != null) {
                taskListContainer.getChildren().add(row);
            }
        }

        updatePaginationUI(totalItems, totalPages, startIndex + (totalItems > 0 ? 1 : 0), endIndex);
        updateFilterUI();
    }

    private void updatePaginationUI(int totalItems, int totalPages, int start, int end) {
        if (paginationSummaryLabel != null) {
            if (totalItems == 0) {
                paginationSummaryLabel.setText("Showing 0 tasks");
            } else {
                paginationSummaryLabel.setText(String.format("Showing %d-%d of %d tasks", start, end, totalItems));
            }
        }
        
        if (paginationContainer != null) {
            paginationContainer.getChildren().clear();
            
            Label prevBtn = new Label("<");
            prevBtn.getStyleClass().add("pagination-btn");
            if (currentPage > 1) {
                prevBtn.setOnMouseClicked(e -> {
                    currentPage--;
                    filterTaskList(searchField != null ? searchField.getText() : "");
                });
            } else {
                prevBtn.setDisable(true);
            }
            paginationContainer.getChildren().add(prevBtn);
            
            for (int i = 1; i <= totalPages; i++) {
                Label pageBtn = new Label(String.valueOf(i));
                pageBtn.getStyleClass().add("pagination-btn");
                if (i == currentPage) {
                    pageBtn.getStyleClass().add("pagination-btn-active");
                }
                final int pageNum = i;
                pageBtn.setOnMouseClicked(e -> {
                    currentPage = pageNum;
                    filterTaskList(searchField != null ? searchField.getText() : "");
                });
                paginationContainer.getChildren().add(pageBtn);
            }
            
            Label nextBtn = new Label(">");
            nextBtn.getStyleClass().add("pagination-btn");
            if (currentPage < totalPages) {
                nextBtn.setOnMouseClicked(e -> {
                    currentPage++;
                    filterTaskList(searchField != null ? searchField.getText() : "");
                });
            } else {
                nextBtn.setDisable(true);
            }
            paginationContainer.getChildren().add(nextBtn);
        }
    }

    private void updateStats(List<Task> filteredTasks) {
        long completed = filteredTasks.stream().filter(t -> t.getStatus() == Task.Status.COMPLETED).count();
        long inProgress = filteredTasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
        long pending = filteredTasks.stream().filter(t -> t.getStatus() == Task.Status.PENDING).count();
        
        if (completedCountLabel != null) completedCountLabel.setText(String.valueOf(completed));
        if (inProgressCountLabel != null) inProgressCountLabel.setText(String.valueOf(inProgress));
        if (pendingCountLabel != null) pendingCountLabel.setText(String.valueOf(pending));
        if (totalCountLabel != null) totalCountLabel.setText(String.valueOf(filteredTasks.size()));
    }

    private boolean matchesQuery(Task task, String query) {
        boolean typeMatch = true;
        LocalDate today = LocalDate.now();
        if ("Today".equals(currentFilterType)) {
            typeMatch = task.getDueDate() != null && task.getDueDate().toLocalDate().isEqual(today);
        } else if ("Upcoming".equals(currentFilterType)) {
            typeMatch = task.getDueDate() != null && task.getDueDate().toLocalDate().isAfter(today);
        } else if ("Completed".equals(currentFilterType)) {
            typeMatch = task.getStatus() == Task.Status.COMPLETED;
        }

        if (!typeMatch) return false;

        if (query == null || query.isBlank()) {
            return true;
        }
        String lowerQuery = query.toLowerCase();
        return task.getTitle().toLowerCase().contains(lowerQuery)
                || (task.getCategory() != null && task.getCategory().toLowerCase().contains(lowerQuery))
                || (task.getTags() != null && task.getTags().stream().anyMatch(t -> t.toLowerCase().contains(lowerQuery)));
    }

    @FXML
    private void handleQuickFilter(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Label) {
            Label clickedLabel = (Label) event.getSource();
            this.currentFilterType = clickedLabel.getText();
            currentPage = 1;
            filterTaskList(searchField != null ? searchField.getText() : "");
        }
    }

    private void updateFilterUI() {
        if (filterAll == null) return;
        Label[] filters = {filterAll, filterToday, filterUpcoming, filterCompleted};
        for (Label l : filters) {
            if (l != null) {
                l.getStyleClass().remove("quick-filter-btn-active");
                if (l.getText().equals(currentFilterType)) {
                    l.getStyleClass().add("quick-filter-btn-active");
                }
            }
        }
    }

    @FXML
    private void handleViewToggle(javafx.scene.input.MouseEvent event) {
        if (event.getSource() instanceof Label) {
            System.out.println("View toggle clicked: " + ((Label)event.getSource()).getText());
        }
    }

    private Node loadTaskRow(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/TaskRowItem.fxml"));
            Node row = loader.load();
            TaskRowController controller = loader.getController();
            if (controller != null) {
                controller.bind(task);
            }
            if (row != null) {
                row.setOnMouseClicked(e -> openEditor(task));
            }
            return row;
        } catch (IOException e) {
            System.err.println("Unable to load task row: " + e.getMessage());
            return null;
        }
    }
}
