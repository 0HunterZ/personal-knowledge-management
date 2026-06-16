package com.focusnode.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LanRoom {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty hostIp = new SimpleStringProperty();
    private final IntegerProperty port = new SimpleIntegerProperty();
    private final IntegerProperty memberCount = new SimpleIntegerProperty(1);
    private final IntegerProperty maxMembers = new SimpleIntegerProperty(10);
    private final ObservableList<LanMember> members = FXCollections.observableArrayList();

    public LanRoom(String id, String name, String hostIp, int port) {
        this.id.set(id);
        this.name.set(name);
        this.hostIp.set(hostIp);
        this.port.set(port);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getName() { return name.get(); }
    public void setName(String name) { this.name.set(name); }
    public StringProperty nameProperty() { return name; }

    public String getHostIp() { return hostIp.get(); }
    public StringProperty hostIpProperty() { return hostIp; }

    public int getPort() { return port.get(); }
    public IntegerProperty portProperty() { return port; }

    public int getMemberCount() { return memberCount.get(); }
    public void setMemberCount(int count) { this.memberCount.set(count); }
    public IntegerProperty memberCountProperty() { return memberCount; }

    public int getMaxMembers() { return maxMembers.get(); }
    public void setMaxMembers(int max) { this.maxMembers.set(max); }
    public IntegerProperty maxMembersProperty() { return maxMembers; }

    public ObservableList<LanMember> getMembers() { return members; }
}
