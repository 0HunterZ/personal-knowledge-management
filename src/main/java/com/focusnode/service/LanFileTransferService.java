package com.focusnode.service;

import com.focusnode.model.LanTransfer;
import javafx.application.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
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
            try (ServerSocket serverSocket = new ServerSocket(0)) {
                // Send metadata to room via sessionService
                int port = serverSocket.getLocalPort();
                
                com.focusnode.model.SyncPacket packet = new com.focusnode.model.SyncPacket();
                packet.setType("FILE_TRANSFER");
                packet.setTransferId(transfer.getId());
                packet.setFileName(file.getName());
                packet.setFileSize(file.length());
                packet.setFileServerPort(port);
                
                sessionService.broadcastFileTransfer(packet);

                // Start accepting the connection (just 1 for now, or multiple if we broadcast)
                // For simplicity, we just accept 1 connection.
                Socket clientSocket = serverSocket.accept();
                
                try (InputStream in = new FileInputStream(file);
                     OutputStream out = clientSocket.getOutputStream()) {
                    
                    long total = file.length();
                    long sent = 0;
                    byte[] buffer = new byte[8192];
                    int read;

                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        sent += read;
                        
                        final double progress = (double) sent / total;
                        Platform.runLater(() -> transfer.setProgress(progress));
                    }
                }
                
                clientSocket.close();
                Platform.runLater(() -> transfer.setStatus("COMPLETED"));
                saveHistory(transfer, "COMPLETED");
            } catch (Exception e) {
                Platform.runLater(() -> transfer.setStatus("FAILED"));
                saveHistory(transfer, "FAILED");
            }
        });
    }

    public void receiveFile(String hostIp, int port, String fileName, long fileSize, String transferId) {
        LanTransfer transfer = new LanTransfer(
                transferId,
                fileName,
                "From " + hostIp,
                fileSize
        );
        transfer.setStatus("TRANSFERRING");

        Platform.runLater(() -> sessionService.getActiveTransfers().add(0, transfer));

        executor.submit(() -> {
            try (Socket socket = new Socket(hostIp, port)) {
                // Determine a safe downloads folder (e.g. user home / Downloads / FocusNode)
                File downloadsDir = new File(System.getProperty("user.home"), "Downloads" + File.separator + "FocusNode");
                if (!downloadsDir.exists()) {
                    downloadsDir.mkdirs();
                }
                
                File outputFile = new File(downloadsDir, fileName);
                
                try (InputStream in = socket.getInputStream();
                     OutputStream out = new FileOutputStream(outputFile)) {
                     
                    long total = fileSize;
                    long received = 0;
                    byte[] buffer = new byte[8192];
                    int read;

                    while ((read = in.read(buffer)) != -1) {
                        out.write(buffer, 0, read);
                        received += read;
                        
                        final double progress = (double) received / total;
                        Platform.runLater(() -> transfer.setProgress(progress));
                        
                        if (received >= total) break;
                    }
                }
                
                Platform.runLater(() -> transfer.setStatus("COMPLETED"));
                saveHistory(transfer, "COMPLETED");
            } catch (Exception e) {
                Platform.runLater(() -> transfer.setStatus("FAILED"));
                saveHistory(transfer, "FAILED");
            }
        });
    }
    
    private void saveHistory(LanTransfer transfer, String status) {
        try {
            com.focusnode.repository.LanTransferHistoryRepository repo = new com.focusnode.repository.LanTransferHistoryRepository();
            com.focusnode.model.LanTransferHistory h = new com.focusnode.model.LanTransferHistory(
                transfer.getId(),
                1, // Default user
                transfer.getFilename(),
                transfer.getTargetName(),
                transfer.getTotalBytes(),
                status,
                java.time.LocalDateTime.now()
            );
            repo.save(h);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
