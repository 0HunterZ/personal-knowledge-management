package com.focusnode.model;

public class SyncPacket {
    private String type; // "JOIN", "LEAVE", "STATUS_UPDATE", "POMODORO_SYNC"
    private LanMemberDto member; // For JOIN, LEAVE, STATUS
    private LanRoomDto roomState; // For initial sync from Host
    private PomodoroSyncState pomodoroState; // Authoritative Pomodoro timer state from Host
    private String pingId;
    private long clientSentAtEpochMillis;
    private long hostSentAtEpochMillis;
    
    private String transferId;
    private String fileName;
    private long fileSize;
    private int fileServerPort;

    private CrdtNote crdtNote; // Payload for CRDT_SYNC

    public SyncPacket() {}

    public SyncPacket(String type, LanMember member) {
        this.type = type;
        this.member = member != null ? new LanMemberDto(member) : null;
    }

    public SyncPacket(String type, LanRoom roomState) {
        this.type = type;
        this.roomState = roomState != null ? new LanRoomDto(roomState) : null;
    }

    public SyncPacket(String type, PomodoroSyncState pomodoroState) {
        this.type = type;
        this.pomodoroState = pomodoroState;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LanMemberDto getMember() { return member; }
    public void setMember(LanMember member) { this.member = member != null ? new LanMemberDto(member) : null; }
    
    public LanRoomDto getRoomState() { return roomState; }
    public void setRoomState(LanRoom roomState) { this.roomState = roomState != null ? new LanRoomDto(roomState) : null; }

    public PomodoroSyncState getPomodoroState() { return pomodoroState; }
    public void setPomodoroState(PomodoroSyncState pomodoroState) { this.pomodoroState = pomodoroState; }

    public String getPingId() { return pingId; }
    public void setPingId(String pingId) { this.pingId = pingId; }

    public long getClientSentAtEpochMillis() { return clientSentAtEpochMillis; }
    public void setClientSentAtEpochMillis(long clientSentAtEpochMillis) {
        this.clientSentAtEpochMillis = clientSentAtEpochMillis;
    }

    public long getHostSentAtEpochMillis() { return hostSentAtEpochMillis; }
    public void setHostSentAtEpochMillis(long hostSentAtEpochMillis) {
        this.hostSentAtEpochMillis = hostSentAtEpochMillis;
    }

    public String getTransferId() { return transferId; }
    public void setTransferId(String transferId) { this.transferId = transferId; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public long getFileSize() { return fileSize; }
    public void setFileSize(long fileSize) { this.fileSize = fileSize; }

    public int getFileServerPort() { return fileServerPort; }
    public void setFileServerPort(int fileServerPort) { this.fileServerPort = fileServerPort; }

    public CrdtNote getCrdtNote() { return crdtNote; }
    public void setCrdtNote(CrdtNote crdtNote) { this.crdtNote = crdtNote; }
}
