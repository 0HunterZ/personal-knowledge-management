package com.focusnode.model;

import javafx.beans.property.*;

public class LanMember {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty ipAddress = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty("Focusing"); // Focusing, Break
    private final LongProperty timeLeftSeconds = new SimpleLongProperty(0);
    private final BooleanProperty isHost = new SimpleBooleanProperty(false);
    private final BooleanProperty isAudioMuted = new SimpleBooleanProperty(false);

    public LanMember(String id, String name, String ipAddress, boolean isHost) {
        this.id.set(id);
        this.name.set(name);
        this.ipAddress.set(ipAddress);
        this.isHost.set(isHost);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getIpAddress() { return ipAddress.get(); }
    public StringProperty ipAddressProperty() { return ipAddress; }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }
    public StringProperty statusProperty() { return status; }

    public long getTimeLeftSeconds() { return timeLeftSeconds.get(); }
    public void setTimeLeftSeconds(long seconds) { this.timeLeftSeconds.set(seconds); }
    public LongProperty timeLeftSecondsProperty() { return timeLeftSeconds; }

    public boolean isHost() { return isHost.get(); }
    public BooleanProperty isHostProperty() { return isHost; }

    public boolean isAudioMuted() { return isAudioMuted.get(); }
    public void setAudioMuted(boolean muted) { this.isAudioMuted.set(muted); }
    public BooleanProperty isAudioMutedProperty() { return isAudioMuted; }
}
