package com.focusnode.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

import java.io.File;
import java.text.DecimalFormat;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StorageCardController {

    @FXML private Label usageLabel;
    @FXML private Label percentageLabel;
    @FXML private ProgressBar progressBar;

    private static final String DB_FILE_PATH = "focusnode.db";
    // Maximum storage capacity allowed (5 GB)
    private static final long MAX_STORAGE_BYTES = 5L * 1024 * 1024 * 1024;
    private ScheduledExecutorService scheduler;

    @FXML
    public void initialize() {
        updateStorageInfo();
        
        // Update storage info every 5 minutes in case database size changes
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::updateStorageInfo, 5, 5, TimeUnit.MINUTES);
    }

    public void updateStorageInfo() {
        File dbFile = new File(DB_FILE_PATH);
        long currentBytes = dbFile.exists() ? dbFile.length() : 0;
        
        // Ensure currentBytes doesn't exceed MAX to avoid > 100% progress
        double progress = Math.min(1.0, (double) currentBytes / MAX_STORAGE_BYTES);
        double percentage = progress * 100;
        
        String usageText = formatSize(currentBytes) + " / 5 GB used";
        String percentageText = (int) percentage + "%";

        Platform.runLater(() -> {
            usageLabel.setText(usageText);
            percentageLabel.setText(percentageText);
            progressBar.setProgress(progress);
        });
    }

    private String formatSize(long bytes) {
        if (bytes <= 0) return "0 MB";
        
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        
        if (digitGroups > 4) {
            digitGroups = 4;
        }
        
        DecimalFormat format = new DecimalFormat("#,##0.#");
        return format.format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
    
    public void shutdown() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }
}
