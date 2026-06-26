package com.focusnode.service;

import com.focusnode.model.FocusSession;
import com.focusnode.model.PomodoroSyncState;
import com.focusnode.model.Task;
import javafx.animation.AnimationTimer;
import javafx.beans.property.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class PomodoroEngine {

    private static final int DEFAULT_FOCUS_MINUTES = 25;
    private static final int DEFAULT_BREAK_MINUTES = 5;
    private static final long SYNC_INTERVAL_MILLIS = 5_000;

    private final ObjectProperty<Task> selectedTask = new SimpleObjectProperty<>();
    private final StringProperty currentPhase = new SimpleStringProperty("Focus");
    private final IntegerProperty secondsRemaining = new SimpleIntegerProperty(DEFAULT_FOCUS_MINUTES * 60);
    private final BooleanProperty isRunning = new SimpleBooleanProperty(false);
    
    private final IntegerProperty focusDuration = new SimpleIntegerProperty(DEFAULT_FOCUS_MINUTES);
    private final IntegerProperty breakDuration = new SimpleIntegerProperty(DEFAULT_BREAK_MINUTES);

    private LocalDateTime sessionStartTime;
    private String sessionId;
    private long deadlineEpochMillis = 0;
    private long lastSyncEpochMillis = 0;

    private AnimationTimer loop;
    private final LanSessionService lanSessionService;

    public PomodoroEngine(LanSessionService lanSessionService) {
        this.lanSessionService = lanSessionService;
        loop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (isRunning.get()) {
                    updateSecondsFromDeadline();
                    if (secondsRemaining.get() <= 0) {
                        handleSessionComplete();
                    }
                }
                publishPeriodicPomodoroState();
            }
        };
        loop.start();
        
        setupRemoteTimerSync();
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

    public void startTimer() {
        if (sessionStartTime == null) {
            sessionStartTime = LocalDateTime.now();
        }
        if (sessionId == null) {
            sessionId = UUID.randomUUID().toString();
        }

        if (secondsRemaining.get() <= 0) {
            resetTimerDuration();
        }

        isRunning.set(true);
        deadlineEpochMillis = System.currentTimeMillis() + secondsRemaining.get() * 1_000L;
        publishPomodoroState();
        publishLocalMemberStatus();
    }

    public void pauseTimer() {
        if (isRunning.get()) {
            updateSecondsFromDeadline();
        }
        isRunning.set(false);
        publishPomodoroState();
        publishLocalMemberStatus();
    }

    public void resetTimer() {
        resetTimerDuration();
        isRunning.set(false);
        deadlineEpochMillis = 0;
        sessionStartTime = null;
        sessionId = null;
        publishPomodoroState();
        publishLocalMemberStatus();
    }

    public void resetTimerDuration() {
        int minutes = currentPhase.get().equals("Break") ? breakDuration.get() : focusDuration.get();
        secondsRemaining.set(Math.max(0, minutes * 60));
    }

    private void handleSessionComplete() {
        pauseTimer();
        publishPomodoroState();
        // Controller will listen to secondsRemaining == 0 to show survey
    }

    private void updateSecondsFromDeadline() {
        if (!isRunning.get() || deadlineEpochMillis <= 0) {
            return;
        }
        long millisRemaining = Math.max(0, deadlineEpochMillis - System.currentTimeMillis());
        secondsRemaining.set((int) Math.ceil(millisRemaining / 1_000.0));
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

    public void publishPomodoroState() {
        if (!lanSessionService.isHostingRoom()) {
            return;
        }

        Task task = selectedTask.get();
        int totalSeconds = currentPhase.get().equals("Break") ? breakDuration.get() * 60 : focusDuration.get() * 60;
        
        String phaseLabel = isRunning.get() ? (currentPhase.get().equals("Break") ? "Break" : "Focusing") : "Paused";
        
        PomodoroSyncState state = new PomodoroSyncState(
                sessionId,
                phaseLabel,
                task == null ? -1 : task.getId(),
                task == null ? currentPhase.get() + " session" : task.getTitle(),
                totalSeconds,
                secondsRemaining.get(),
                isRunning.get(),
                isRunning.get() ? deadlineEpochMillis : 0,
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

        String currentStatus = isRunning.get() ? "Focusing" : "Paused";
        long timeLeftSeconds = Math.max(0, secondsRemaining.get());
        lanSessionService.publishLocalStatus(currentStatus, timeLeftSeconds);
    }
    
    private void applyRemoteTimerState(PomodoroSyncState state) {
        if (state == null) {
            return;
        }

        sessionId = state.getSessionId();
        
        String phaseStr = state.getPhase();
        if (phaseStr != null && (phaseStr.contains("Break") || phaseStr.contains("Focusing"))) {
            currentPhase.set(phaseStr.contains("Break") ? "Break" : "Focus");
        }

        long nowOnHostClock = System.currentTimeMillis() + lanSessionService.getHostClockOffsetMillis();
        isRunning.set(state.isRunning());

        if (isRunning.get() && state.getDeadlineEpochMillis() > 0) {
            long remainingMillis = Math.max(0, state.getDeadlineEpochMillis() - nowOnHostClock);
            secondsRemaining.set((int) Math.ceil(remainingMillis / 1_000.0));
        } else {
            long elapsedMillis = Math.max(0, nowOnHostClock - state.getCreatedAtEpochMillis());
            int elapsedSeconds = (int) Math.floor(elapsedMillis / 1_000.0);
            secondsRemaining.set(Math.max(0, state.getRemainingSeconds() - elapsedSeconds));
        }

        deadlineEpochMillis = isRunning.get() ? System.currentTimeMillis() + secondsRemaining.get() * 1_000L : 0;
    }
    
    // Getters and Properties

    public ObjectProperty<Task> selectedTaskProperty() { return selectedTask; }
    public StringProperty currentPhaseProperty() { return currentPhase; }
    public IntegerProperty secondsRemainingProperty() { return secondsRemaining; }
    public BooleanProperty isRunningProperty() { return isRunning; }
    
    public IntegerProperty focusDurationProperty() { return focusDuration; }
    public IntegerProperty breakDurationProperty() { return breakDuration; }

    public LocalDateTime getSessionStartTime() { return sessionStartTime; }
}
