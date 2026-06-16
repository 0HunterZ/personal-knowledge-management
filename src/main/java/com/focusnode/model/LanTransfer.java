package com.focusnode.model;

import javafx.beans.property.*;

public class LanTransfer {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty filename = new SimpleStringProperty();
    private final StringProperty targetName = new SimpleStringProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty(0.0);
    private final StringProperty status = new SimpleStringProperty("PENDING"); // PENDING, TRANSFERRING, PAUSED, COMPLETED, FAILED
    private final LongProperty totalBytes = new SimpleLongProperty(0);

    public LanTransfer(String id, String filename, String targetName, long totalBytes) {
        this.id.set(id);
        this.filename.set(filename);
        this.targetName.set(targetName);
        this.totalBytes.set(totalBytes);
    }

    public String getId() { return id.get(); }
    
    public String getFilename() { return filename.get(); }
    public StringProperty filenameProperty() { return filename; }

    public String getTargetName() { return targetName.get(); }
    public StringProperty targetNameProperty() { return targetName; }

    public double getProgress() { return progress.get(); }
    public void setProgress(double p) { this.progress.set(p); }
    public DoubleProperty progressProperty() { return progress; }

    public String getStatus() { return status.get(); }
    public void setStatus(String s) { this.status.set(s); }
    public StringProperty statusProperty() { return status; }

    public long getTotalBytes() { return totalBytes.get(); }
    public LongProperty totalBytesProperty() { return totalBytes; }
}
