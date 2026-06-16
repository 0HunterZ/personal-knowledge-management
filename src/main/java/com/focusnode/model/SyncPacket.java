package com.focusnode.model;

public class SyncPacket {
    private String type; // "JOIN", "LEAVE", "TICK", "STATUS"
    private LanMember member; // For JOIN, LEAVE, STATUS
    private LanRoom roomState; // For initial sync from Host

    public SyncPacket() {}

    public SyncPacket(String type, LanMember member) {
        this.type = type;
        this.member = member;
    }

    public SyncPacket(String type, LanRoom roomState) {
        this.type = type;
        this.roomState = roomState;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LanMember getMember() { return member; }
    public void setMember(LanMember member) { this.member = member; }

    public LanRoom getRoomState() { return roomState; }
    public void setRoomState(LanRoom roomState) { this.roomState = roomState; }
}
