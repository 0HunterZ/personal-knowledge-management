package com.focusnode.controller;

import com.focusnode.model.FocusSession;
import com.focusnode.model.PomodoroSyncState;
import com.focusnode.model.Task;
import com.focusnode.service.LanSessionService;
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
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class ZenModeController {

    @FXML private ComboBox<Task> taskComboBox;
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

    private static final int POMODORO_MINUTES = 25;
    private static final int TOTAL_SECONDS = POMODORO_MINUTES * 60;
    private static final long SYNC_INTERVAL_MILLIS = 5_000;

    private final LanSessionService lanSessionService = ServiceLocator.getLanSessionService();

    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> timerTask;
    private LocalDateTime sessionStartTime;
    private Task selectedTask;
    private String sessionId;
    private int secondsRemaining = TOTAL_SECONDS;
    private boolean isRunning = false;
    private long deadlineEpochMillis = 0;
    private long lastSyncEpochMillis = 0;

    @FXML
    public void initialize() {
        scheduler = Executors.newScheduledThreadPool(1, Thread.ofVirtual().factory());

        bindIncludedNodes();
        setupTaskSelector();
        setupRemoteTimerSync();
        loadTasks();
        updateTimerDisplay();
    }

    private void bindIncludedNodes() {
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
        taskComboBox.valueProperty().addListener((obs, oldVal, newVal) -> setSelectedTask(newVal));
    }

    private void setupRemoteTimerSync() {
        lanSessionService.pomodoroStateProperty().addListener((obs, oldState, newState) -> {
            if (!lanSessionService.isHostingRoom()) {
                applyRemoteTimerState(newState);
            }
        });

        if (!lanSessionService.isHostingRoom()) {
            applyRemoteTimerState(lanSessionService.getPomodoroState());
        }
    }

    private void loadTasks() {
        ServiceLocator.getAsyncExecutor().execute(() -> {
            List<Task> tasks = ServiceLocator.getAppDataService().getTasks().stream()
                    .filter(task -> task.getStatus() != Task.Status.COMPLETED)
                    .toList();
            Platform.runLater(() -> {
                if (taskComboBox != null) {
                    taskComboBox.setItems(FXCollections.observableArrayList(tasks));
                    if (!tasks.isEmpty()) {
                        taskComboBox.getSelectionModel().selectFirst();
                    }
                } else if (!tasks.isEmpty()) {
                    setSelectedTask(tasks.get(0));
                }
            });
        });
    }

    private void setSelectedTask(Task task) {
        selectedTask = task;
        if (taskTitleLabel != null) {
            taskTitleLabel.setText(task == null ? "Choose a focus task" : task.getTitle());
        }
        if (taskCategoryLabel != null) {
            taskCategoryLabel.setText(task == null ? "Focus" : task.getCategory());
            taskCategoryLabel.setVisible(task != null);
        }
    }

    @FXML
    private void toggleTimer() {
        if (!canControlTimer()) {
            return;
        }

        if (isRunning) {
            pauseTimer();
        } else {
            startTimer();
        }
    }

    private void startTimer() {
        cancelTimerTask();
        if (sessionStartTime == null) {
            sessionStartTime = LocalDateTime.now();
        }
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }

        isRunning = true;
        deadlineEpochMillis = System.currentTimeMillis() + secondsRemaining * 1_000L;
        if (playPauseButton != null) playPauseButton.setText("||");
        if (statusLabel != null) {
            statusLabel.setText("Focusing");
            applyStatusLabelStyle();
        }
        publishPomodoroState();
        publishLocalMemberStatus();

        timerTask = scheduler.scheduleAtFixedRate(() -> {
            updateSecondsFromDeadline();
            publishLocalMemberStatus();
            publishPeriodicPomodoroState();
            Platform.runLater(() -> {
                updateTimerDisplay();
                if (secondsRemaining <= 0) {
                    handleSessionComplete();
                }
            });
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void pauseTimer() {
        if (isRunning) {
            updateSecondsFromDeadline();
        }
        isRunning = false;
        cancelTimerTask();
        if (playPauseButton != null) playPauseButton.setText(">");
        if (statusLabel != null) {
            statusLabel.setText("Paused");
            applyStatusLabelStyle();
        }
        publishPomodoroState();
        publishLocalMemberStatus();
    }

    private void updateTimerDisplay() {
        int min = secondsRemaining / 60;
        int sec = secondsRemaining % 60;
        if (timerLabel != null) {
            timerLabel.setText(String.format("%02d:%02d", min, sec));
        }
        if (sessionLabel != null) {
            sessionLabel.setText("Session 1 of 4");
        }
        if (endsAtLabel != null) {
            if (isRunning) {
                LocalTime endTime = LocalTime.now().plusSeconds(secondsRemaining);
                endsAtLabel.setText("Ends at " + endTime.format(DateTimeFormatter.ofPattern("hh:mm a")));
            } else {
                endsAtLabel.setText("Ready when you are");
            }
        }

        if (progressCircle != null) {
            double progress = 1.0 - ((double) secondsRemaining / TOTAL_SECONDS);
            double circumference = 2 * Math.PI * 112;
            progressCircle.setStrokeDashOffset(circumference * (1.0 - progress));
        }
    }

    private void handleSessionComplete() {
        pauseTimer();
        if (statusLabel != null) statusLabel.setText("Session Complete!");
        publishPomodoroState();
        showSurveyDialog();
    }

    @FXML
    private void endSession() {
        if (!canControlTimer()) {
            return;
        }

        pauseTimer();
        publishLocalMemberStatus();
        if (secondsRemaining < TOTAL_SECONDS) {
            showSurveyDialog();
        } else {
            resetTimer();
        }
    }

    @FXML
    private void exitZenMode() {
        endSession();
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
                resetTimer();
            });

            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveSessionData() {
        Task task = getSelectedTask();
        int minutesSpent = (TOTAL_SECONDS - secondsRemaining) / 60;

        if (sessionStartTime == null || minutesSpent <= 0) {
            return;
        }

        if (task != null) {
            task.setActualMinutes(task.getActualMinutes() + minutesSpent);
            ServiceLocator.getAppDataService().saveTask(task);
        }

        FocusSession session = new FocusSession(
                -1,
                1,
                task != null ? task.getId() : -1,
                -1,
                -1,
                sessionStartTime,
                LocalDateTime.now(),
                POMODORO_MINUTES,
                minutesSpent,
                minutesSpent >= POMODORO_MINUTES
        );

        ServiceLocator.getAsyncExecutor().execute(() -> {
            ServiceLocator.getAppDataService().saveFocusSession(session);
            System.out.println("Saved Focus Session: " + session.getActualMinutes() + " mins");
        });
    }

    private void resetTimer() {
        cancelTimerTask();
        secondsRemaining = TOTAL_SECONDS;
        isRunning = false;
        deadlineEpochMillis = 0;
        sessionStartTime = null;
        sessionId = null;
        if (statusLabel != null) {
            statusLabel.setText("Ready");
            applyStatusLabelStyle();
        }
        if (playPauseButton != null) playPauseButton.setText(">");
        updateTimerDisplay();
        publishPomodoroState();
        publishLocalMemberStatus();
    }

    private Task getSelectedTask() {
        if (taskComboBox != null) {
            return taskComboBox.getValue();
        }
        return selectedTask;
    }

    private boolean canControlTimer() {
        return lanSessionService.getActiveRoom() == null || lanSessionService.isHostingRoom();
    }

    private void updateSecondsFromDeadline() {
        if (!isRunning || deadlineEpochMillis <= 0) {
            return;
        }
        long millisRemaining = Math.max(0, deadlineEpochMillis - System.currentTimeMillis());
        secondsRemaining = (int) Math.ceil(millisRemaining / 1_000.0);
    }

    private void publishPeriodicPomodoroState() {
        if (!lanSessionService.isHostingRoom()) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastSyncEpochMillis >= SYNC_INTERVAL_MILLIS) {
            publishPomodoroState();
        }
    }

    private void publishPomodoroState() {
        if (!lanSessionService.isHostingRoom()) {
            return;
        }

        Task task = getSelectedTask();
        PomodoroSyncState state = new PomodoroSyncState(
                sessionId,
                statusLabel == null ? "Focusing" : statusLabel.getText(),
                task == null ? -1 : task.getId(),
                task == null ? "Focus session" : task.getTitle(),
                TOTAL_SECONDS,
                secondsRemaining,
                isRunning,
                isRunning ? deadlineEpochMillis : 0,
                lanSessionService.nextPomodoroSequence(),
                System.currentTimeMillis()
        );
        lastSyncEpochMillis = state.getCreatedAtEpochMillis();
        lanSessionService.publishPomodoroState(state);
    }

    private void publishLocalMemberStatus() {
        if (lanSessionService.getActiveRoom() == null) {
            return;
        }

        String currentStatus = isRunning ? "Focusing" : "Paused";
        long timeLeftSeconds = Math.max(0, secondsRemaining);
        lanSessionService.publishLocalStatus(currentStatus, timeLeftSeconds);
    }

    private void applyRemoteTimerState(PomodoroSyncState state) {
        if (state == null) {
            return;
        }

        cancelTimerTask();
        sessionId = state.getSessionId();
        long nowOnHostClock = System.currentTimeMillis() + lanSessionService.getHostClockOffsetMillis();
        isRunning = state.isRunning();

        if (isRunning && state.getDeadlineEpochMillis() > 0) {
            long remainingMillis = Math.max(0, state.getDeadlineEpochMillis() - nowOnHostClock);
            secondsRemaining = (int) Math.ceil(remainingMillis / 1_000.0);
        } else {
            long elapsedMillis = Math.max(0, nowOnHostClock - state.getCreatedAtEpochMillis());
            int elapsedSeconds = (int) Math.floor(elapsedMillis / 1_000.0);
            secondsRemaining = Math.max(0, state.getRemainingSeconds() - elapsedSeconds);
        }

        deadlineEpochMillis = isRunning ? System.currentTimeMillis() + secondsRemaining * 1_000L : 0;

        if (taskTitleLabel != null) {
            taskTitleLabel.setText(state.getTaskTitle());
        }
        if (taskCategoryLabel != null) {
            taskCategoryLabel.setText("LAN Sync");
            taskCategoryLabel.setVisible(true);
        }
        if (statusLabel != null) {
            statusLabel.setText(isRunning ? state.getPhase() : "Paused");
            applyStatusLabelStyle();
        }
        if (playPauseButton != null) {
            playPauseButton.setText(isRunning ? "||" : ">");
            playPauseButton.setDisable(true);
        }

        if (isRunning) {
            timerTask = scheduler.scheduleAtFixedRate(() -> Platform.runLater(() -> {
                updateSecondsFromDeadline();
                updateTimerDisplay();
            }), 0, 1, TimeUnit.SECONDS);
        } else {
            updateTimerDisplay();
        }
    }

    private void cancelTimerTask() {
        if (timerTask != null) {
            timerTask.cancel(false);
            timerTask = null;
        }
    }
}
