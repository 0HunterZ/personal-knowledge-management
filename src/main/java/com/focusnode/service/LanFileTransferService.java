package com.focusnode.service;

import com.focusnode.model.LanTransfer;
import javafx.application.Platform;

import java.io.File;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

public class LanFileTransferService {

    private final LanSessionService sessionService;
    private final ExecutorService executor;

    public LanFileTransferService(LanSessionService sessionService, ExecutorService executor) {
        this.sessionService = sessionService;
        this.executor = executor;
    }

    public void sendFile(File file, String targetMemberName) {
        LanTransfer transfer = new LanTransfer(
                UUID.randomUUID().toString(),
                file.getName(),
                targetMemberName,
                file.length()
        );
        transfer.setStatus("TRANSFERRING");

        Platform.runLater(() -> sessionService.getActiveTransfers().add(0, transfer));

        executor.submit(() -> {
            try {
                // Simulate file transfer progress
                long total = file.length();
                long sent = 0;
                long chunkSize = Math.max(total / 20, 1024 * 1024); // 5% or 1MB chunks

                while (sent < total) {
                    Thread.sleep(200); // Network delay
                    sent += chunkSize;
                    if (sent > total) sent = total;
                    
                    final double progress = (double) sent / total;
                    Platform.runLater(() -> transfer.setProgress(progress));
                }
                
                Platform.runLater(() -> transfer.setStatus("COMPLETED"));
            } catch (Exception e) {
                Platform.runLater(() -> transfer.setStatus("FAILED"));
            }
        });
    }
}
