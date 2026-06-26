package com.focusnode.controller;

import com.focusnode.model.FocusSession;
import com.focusnode.model.Task;
import com.focusnode.service.LanSessionService;
import com.focusnode.service.PomodoroEngine;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ZenModeController {

    @FXML private ComboBox<Task> taskComboBox;
    @FXML private ComboBox<String> phaseComboBox;
    @FXML private Spinner<Integer> focusDurationSpinner;
    @FXML private Spinner<Integer> breakDurationSpinner;
    @FXML private Label taskTitleLabel;
    @FXML private Label taskCategoryLabel;
    @FXML private Label timerLabel;
    @FXML private Label statusLabel;
    @FXML private Label sessionLabel;
    @FXML private Label endsAtLabel;
    @FXML private Circle progressCircle;
    @FXML private Button playPauseButton;
    @FXML private Parent zenHeader;
    @FXML private Parent zenTimerPanel;
    @FXML private Parent zenControls;

    private final LanSessionService lanSessionService = ServiceLocator.getLanSessionService();
    private PomodoroEngine engine;
    private final com.focusnode.service.AudioService audioService = new com.focusnode.service.AudioService();

    @FXML
    public void initialize() {
        engine = ServiceLocator.getPomodoroEngine();

        bindIncludedNodes();
        setupTaskSelector();
        setupPomodoroControls();
        
        // Listeners to engine properties
        engine.secondsRemainingProperty().addListener((obs, oldVal, newVal) -> updateTimerDisplay());
        engine.isRunningProperty().addListener((obs, oldVal, newVal) -> updatePlayPauseButton());
        engine.currentPhaseProperty().addListener((obs, oldVal, newVal) -> {
            if (phaseComboBox != null) {
                phaseComboBox.getSelectionModel().select(newVal);
            }
            updateTimerDisplay();
        });
        engine.selectedTaskProperty().addListener((obs, oldVal, newVal) -> updateTaskLabels(newVal));

        // When countdown reaches 0, show survey dialog (only if transitioning from Focus -> completed)
        engine.isRunningProperty().addListener((obs, wasRunning, isRunningNow) -> {
            if (wasRunning && !isRunningNow && engine.secondsRemainingProperty().get() <= 0) {
                if (!"Break".equals(engine.currentPhaseProperty().get())) {
                    showSurveyDialog();
                } else {
                    if (statusLabel != null) {
                        statusLabel.setText("Break Complete!");
                        applyStatusLabelStyle();
                    }
                }
            }
        });

        loadTasks();
        updateTimerDisplay();
        updatePlayPauseButton();
        updateTaskLabels(engine.selectedTaskProperty().get());
        
        // Setup initial status label based on engine state
        if (statusLabel != null) {
            String currentPhase = engine.currentPhaseProperty().get();
            statusLabel.setText(engine.isRunningProperty().get() ? ("Break".equals(currentPhase) ? "Break" : "Focusing") : "Paused");
            applyStatusLabelStyle();
        }
    }

    private void setupPomodoroControls() {
        if (phaseComboBox != null) {
            phaseComboBox.setItems(FXCollections.observableArrayList("Focus", "Break"));
            phaseComboBox.getSelectionModel().select(engine.currentPhaseProperty().get());
            phaseComboBox.valueProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null && !newValue.equals(engine.currentPhaseProperty().get())) {
                    engine.currentPhaseProperty().set(newValue);
                    engine.resetTimerDuration();
                }
            });
        }

        if (focusDurationSpinner != null) {
            focusDurationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 90, engine.focusDurationProperty().get(), 5));
            focusDurationSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                engine.focusDurationProperty().set(newValue);
                if ("Focus".equals(engine.currentPhaseProperty().get())) {
                    engine.resetTimerDuration();
                }
            });
        }

        if (breakDurationSpinner != null) {
            breakDurationSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 30, engine.breakDurationProperty().get(), 1));
            breakDurationSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
                engine.breakDurationProperty().set(newValue);
                if ("Break".equals(engine.currentPhaseProperty().get())) {
                    engine.resetTimerDuration();
                }
            });
        }
    }

    private void bindIncludedNodes() {
        taskComboBox = lookupIncludedNode(zenTimerPanel, "taskComboBox", ComboBox.class, taskComboBox);
        phaseComboBox = lookupIncludedNode(zenTimerPanel, "phaseComboBox", ComboBox.class, phaseComboBox);
        focusDurationSpinner = lookupIncludedNode(zenTimerPanel, "focusDurationSpinner", Spinner.class, focusDurationSpinner);
        breakDurationSpinner = lookupIncludedNode(zenTimerPanel, "breakDurationSpinner", Spinner.class, breakDurationSpinner);
        taskTitleLabel = lookupIncludedNode(zenTimerPanel, "taskTitleLabel", Label.class, taskTitleLabel);
        taskCategoryLabel = lookupIncludedNode(zenTimerPanel, "taskCategoryLabel", Label.class, taskCategoryLabel);
        timerLabel = lookupIncludedNode(zenTimerPanel, "timerLabel", Label.class, timerLabel);
        statusLabel = lookupIncludedNode(zenTimerPanel, "statusLabel", Label.class, statusLabel);
        sessionLabel = lookupIncludedNode(zenTimerPanel, "sessionLabel", Label.class, sessionLabel);
        endsAtLabel = lookupIncludedNode(zenTimerPanel, "endsAtLabel", Label.class, endsAtLabel);
        progressCircle = lookupIncludedNode(zenTimerPanel, "progressCircle", Circle.class, progressCircle);

        playPauseButton = lookupIncludedNode(zenControls, "playPauseButton", Button.class, playPauseButton);
        if (playPauseButton != null) {
            playPauseButton.setOnAction(event -> toggleTimer());
        }

        Button endSessionButton = lookupIncludedNode(zenControls, "endSessionButton", Button.class, null);
        if (endSessionButton != null) {
            endSessionButton.setOnAction(event -> endSession());
        }

        Button exitZenButton = lookupIncludedNode(zenHeader, "exitZenButton", Button.class, null);
        if (exitZenButton != null) {
            exitZenButton.setOnAction(event -> exitZenMode());
        }
    }

    private <T> T lookupIncludedNode(Parent root, String id, Class<T> type, T fallback) {
        if (fallback != null || root == null) {
            return fallback;
        }

        javafx.scene.Node node = root.lookup("#" + id);
        if (type.isInstance(node)) {
            return type.cast(node);
        }
        return null;
    }

    private void setupTaskSelector() {
        if (taskComboBox == null) {
            return;
        }

        taskComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Task task) {
                return task == null ? "" : task.getTitle();
            }

            @Override
            public Task fromString(String string) {
                return null;
            }
        });
        taskComboBox.valueProperty().addListener((obs, oldVal, newVal) -> engine.selectedTaskProperty().set(newVal));
    }

    private void loadTasks() {
        ServiceLocator.getAsyncExecutor().execute(() -> {
            List<Task> tasks = ServiceLocator.getAppDataService().getTasks().stream()
                    .filter(task -> task.getStatus() != Task.Status.COMPLETED)
                    .toList();
            Platform.runLater(() -> {
                if (taskComboBox != null) {
                    taskComboBox.setItems(FXCollections.observableArrayList(tasks));
                    if (!tasks.isEmpty() && engine.selectedTaskProperty().get() == null) {
                        taskComboBox.getSelectionModel().selectFirst();
                    } else if (engine.selectedTaskProperty().get() != null) {
                        taskComboBox.getSelectionModel().select(engine.selectedTaskProperty().get());
                    }
                } else if (!tasks.isEmpty() && engine.selectedTaskProperty().get() == null) {
                    engine.selectedTaskProperty().set(tasks.get(0));
                }
            });
        });
    }

    private void updateTaskLabels(Task task) {
        if (taskTitleLabel != null) {
            if (task == null) {
                taskTitleLabel.setText("Break".equals(engine.currentPhaseProperty().get()) ? "Ready for a break" : "Choose a focus task");
            } else {
                taskTitleLabel.setText(task.getTitle());
            }
        }
        if (taskCategoryLabel != null) {
            if (task == null) {
                taskCategoryLabel.setVisible(!"Break".equals(engine.currentPhaseProperty().get()));
                taskCategoryLabel.setText("Focus");
            } else {
                taskCategoryLabel.setText(task.getCategory());
                taskCategoryLabel.setVisible(true);
            }
        }
        
        // Make sure remote task is also properly annotated when in remote sync
        if (!canControlTimer() && taskTitleLabel != null) {
            taskCategoryLabel.setText("LAN Sync");
            taskCategoryLabel.setVisible(true);
        }
    }

    @FXML
    private void toggleTimer() {
        if (!canControlTimer()) return;

        if (engine.isRunningProperty().get()) {
            engine.pauseTimer();
            // Pause OS Zen Mode
            com.focusnode.util.OSManager.disableDoNotDisturb();
            com.focusnode.util.OSManager.unblockDistractingWebsites();
            audioService.stopBinauralBeats();
        } else {
            engine.startTimer();
            // Start OS Zen Mode
            if ("Focus".equals(engine.currentPhaseProperty().get())) {
                com.focusnode.util.OSManager.enableDoNotDisturb();
                com.focusnode.util.OSManager.blockDistractingWebsites();
                audioService.playBinauralBeats();
            }
        }
        
        if (statusLabel != null) {
            String currentPhase = engine.currentPhaseProperty().get();
            statusLabel.setText(engine.isRunningProperty().get() ? ("Break".equals(currentPhase) ? "Break" : "Focusing") : "Paused");
            applyStatusLabelStyle();
        }
    }

    private void updatePlayPauseButton() {
        if (playPauseButton != null) {
            playPauseButton.setText(engine.isRunningProperty().get() ? "||" : ">");
            playPauseButton.setDisable(!canControlTimer() && lanSessionService.getActiveRoom() != null);
        }
        if (statusLabel != null) {
            String phase = engine.currentPhaseProperty().get();
            if (engine.secondsRemainingProperty().get() <= 0) {
                 statusLabel.setText("Break".equals(phase) ? "Break Complete!" : "Focus Complete!");
            } else {
                 statusLabel.setText(engine.isRunningProperty().get() ? ("Break".equals(phase) ? "Break" : "Focusing") : "Paused");
            }
            applyStatusLabelStyle();
        }
    }

    private void updateTimerDisplay() {
        int secondsRemaining = engine.secondsRemainingProperty().get();
        String currentPhase = engine.currentPhaseProperty().get();
        
        int min = secondsRemaining / 60;
        int sec = secondsRemaining % 60;
        if (timerLabel != null) {
            timerLabel.setText(String.format("%02d:%02d", min, sec));
        }
        if (sessionLabel != null) {
            sessionLabel.setText(currentPhase + " Session");
        }
        if (endsAtLabel != null) {
            if (engine.isRunningProperty().get()) {
                LocalTime endTime = LocalTime.now().plusSeconds(secondsRemaining);
                endsAtLabel.setText("Ends at " + endTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
            } else {
                endsAtLabel.setText("Ready when you are");
            }
        }

        if (progressCircle != null) {
            int totalSeconds = "Break".equals(currentPhase) ? engine.breakDurationProperty().get() * 60 : engine.focusDurationProperty().get() * 60;
            double progress = 1.0 - ((double) secondsRemaining / Math.max(1, totalSeconds));
            double circumference = 2 * Math.PI * 112;
            progressCircle.setStrokeDashOffset(circumference * (1.0 - progress));
        }
        applyStatusLabelStyle();
    }

    @FXML
    private void endSession() {
        if (!canControlTimer()) {
            return;
        }

        engine.pauseTimer();
        
        // Stop OS Zen Mode
        com.focusnode.util.OSManager.disableDoNotDisturb();
        com.focusnode.util.OSManager.unblockDistractingWebsites();
        audioService.stopBinauralBeats();

        int totalSeconds = "Break".equals(engine.currentPhaseProperty().get()) ? engine.breakDurationProperty().get() * 60 : engine.focusDurationProperty().get() * 60;
        if (engine.secondsRemainingProperty().get() < totalSeconds) {
            showSurveyDialog();
        } else {
            engine.resetTimer();
        }
    }

    @FXML
    private void exitZenMode() {
        // Just navigate away; DO NOT END SESSION!
        // The Pomodoro engine will keep running in the background.
        // We do NOT disable DND here because they are still in a session,
        // unless you want exiting the view to break Zen Mode. Let's keep Zen active.
        com.focusnode.navigation.AppNavigator.navigateTo(com.focusnode.navigation.AppView.HOME);
    }

    private void showSurveyDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/SurveyDialog.fxml"));
            Parent root = loader.load();
            SurveyDialogController controller = loader.getController();

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Session Survey");
            stage.setScene(new Scene(root));

            controller.initData(stage, result -> {
                saveSessionData();
                engine.resetTimer();
                if (statusLabel != null) {
                    statusLabel.setText("Ready");
                    applyStatusLabelStyle();
                }
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSessionData() {
        Task task = engine.selectedTaskProperty().get();
        String currentPhase = engine.currentPhaseProperty().get();
        int totalSeconds = "Break".equals(currentPhase) ? engine.breakDurationProperty().get() * 60 : engine.focusDurationProperty().get() * 60;
        int minutesSpent = (totalSeconds - engine.secondsRemainingProperty().get()) / 60;

        if (engine.getSessionStartTime() == null || minutesSpent <= 0) {
            return;
        }

        if (task != null) {
            task.setActualMinutes(task.getActualMinutes() + minutesSpent);
            ServiceLocator.getAppDataService().saveTask(task);
        }

        int plannedMinutes = "Break".equals(currentPhase) ? engine.breakDurationProperty().get() : engine.focusDurationProperty().get();
        FocusSession session = new FocusSession(
                -1,
                1,
                task != null ? task.getId() : -1,
                -1,
                -1,
                engine.getSessionStartTime(),
                LocalDateTime.now(),
                plannedMinutes,
                minutesSpent,
                minutesSpent >= plannedMinutes
        );

        ServiceLocator.getAsyncExecutor().execute(() -> {
            ServiceLocator.getAppDataService().saveFocusSession(session);
            System.out.println("Saved Focus Session: " + session.getActualMinutes() + " mins");
        });
    }

    private void applyStatusLabelStyle() {
        if (statusLabel == null) {
            return;
        }
        statusLabel.getStyleClass().removeAll("zen-status-focus", "zen-status-break");
        String state = statusLabel.getText();
        if ("Break".equalsIgnoreCase(state) || "Break Complete!".equalsIgnoreCase(state)) {
            statusLabel.getStyleClass().add("zen-status-break");
        } else if ("Focusing".equalsIgnoreCase(state) || "Focus Complete!".equalsIgnoreCase(state)) {
            statusLabel.getStyleClass().add("zen-status-focus");
        }
    }

    private boolean canControlTimer() {
        return lanSessionService.getActiveRoom() == null || lanSessionService.isHostingRoom();
    }
}
