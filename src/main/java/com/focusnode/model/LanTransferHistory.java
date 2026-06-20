package com.focusnode.model;

import java.time.LocalDateTime;

public class LanTransferHistory {
    private String transferId;
    private int userId;
    private String fileName;
    private String targetName;
    private long sizeBytes;
    private String status;
    private LocalDateTime createdAt;

    public LanTransferHistory() {
    }

    public LanTransferHistory(String transferId, int userId, String fileName, String targetName, long sizeBytes, String status, LocalDateTime createdAt) {
        this.transferId = transferId;
        this.userId = userId;
        this.fileName = fileName;
        this.targetName = targetName;
        this.sizeBytes = sizeBytes;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getTargetName() { return targetName; }
    public void setTargetName(String targetName) { this.targetName = targetName; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
