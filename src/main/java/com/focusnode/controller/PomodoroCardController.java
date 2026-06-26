package com.focusnode.controller;

import com.focusnode.navigation.AppNavigator;
import com.focusnode.navigation.AppView;
import com.focusnode.service.PomodoroEngine;
import com.focusnode.service.ServiceLocator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.shape.Arc;

public class PomodoroCardController {

    @FXML private Label timeLabel;
    @FXML private Label phaseLabel;
    @FXML private Arc progressArc;
    @FXML private Button toggleButton;

    private PomodoroEngine engine;

    @FXML
    public void initialize() {
        engine = ServiceLocator.getPomodoroEngine();
        
        engine.secondsRemainingProperty().addListener((obs, oldVal, newVal) -> updateTimerDisplay());
        engine.currentPhaseProperty().addListener((obs, oldVal, newVal) -> updateTimerDisplay());
        engine.isRunningProperty().addListener((obs, oldVal, newVal) -> updateTimerDisplay());
        
        updateTimerDisplay();
    }

    private void updateTimerDisplay() {
        Platform.runLater(() -> {
            int totalSeconds = engine.currentPhaseProperty().get().equals("Break") ? 
                               engine.breakDurationProperty().get() * 60 : 
                               engine.focusDurationProperty().get() * 60;
            
            int remaining = engine.secondsRemainingProperty().get();
            int min = remaining / 60;
            int sec = remaining % 60;
            
            if (timeLabel != null) {
                timeLabel.setText(String.format("%02d:%02d", min, sec));
            }
            if (phaseLabel != null) {
                phaseLabel.setText(engine.currentPhaseProperty().get());
            }
            if (toggleButton != null) {
                toggleButton.setText(engine.isRunningProperty().get() ? "⏸ Pause" : "▶ Start " + engine.currentPhaseProperty().get());
            }
            if (progressArc != null) {
                double fraction = totalSeconds > 0 ? (double) remaining / totalSeconds : 0;
                progressArc.setLength(-360 * fraction);
            }
        });
    }

    @FXML
    private void toggleTimer() {
        if (engine.isRunningProperty().get()) {
            engine.pauseTimer();
        } else {
            engine.startTimer();
        }
    }

    @FXML
    private void enterZenMode() {
        AppNavigator.navigateTo(AppView.ZEN_MODE);
    }
}
