package com.focusnode.model;

public class DiscoveryPacket {
    private String roomId;
    private String roomName;
    private String hostIp;
    private int port;
    private int memberCount;
    private int maxMembers;

    public DiscoveryPacket() {}

    public DiscoveryPacket(LanRoom room) {
        this.roomId = room.getId();
        this.roomName = room.getName();
        this.hostIp = room.getHostIp();
        this.port = room.getPort();
        this.memberCount = room.getMemberCount();
        this.maxMembers = room.getMaxMembers();
    }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getHostIp() { return hostIp; }
    public void setHostIp(String hostIp) { this.hostIp = hostIp; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public int getMemberCount() { return memberCount; }
    public void setMemberCount(int memberCount) { this.memberCount = memberCount; }

    public int getMaxMembers() { return maxMembers; }
    public void setMaxMembers(int maxMembers) { this.maxMembers = maxMembers; }
}
