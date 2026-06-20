package com.focusnode.service;

import com.focusnode.model.LanMember;
import com.focusnode.model.LanRoom;
import com.focusnode.model.LanTransfer;
import com.focusnode.model.PomodoroSyncState;
import com.focusnode.model.SyncPacket;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LanSessionService {
    
    // Observable list of discovered rooms for the Discover card
    private final ObservableList<LanRoom> discoveredRooms = FXCollections.observableArrayList();
    
    // The currently active room the user is in (null if not in a room)
    private final ObjectProperty<LanRoom> activeRoom = new SimpleObjectProperty<>(null);
    
    // Active file transfers
    private final ObservableList<LanTransfer> activeTransfers = FXCollections.observableArrayList();

    private final ObjectProperty<PomodoroSyncState> pomodoroState = new SimpleObjectProperty<>(null);
    private long pomodoroSequence = 0;
    private Long hostClockOffsetMillis = null;

    private LanSessionServer server;
    private LanSessionClient client;
    private LanMember localMember;

    public LanSessionService() {
        // No mock data loaded by default
    }

    public void hostRoom(LanRoom room) {
        activeRoom.set(room);
        localMember = room.getMembers().stream()
                .filter(LanMember::isHost)
                .findFirst()
                .orElse(null);
        room.getActivities().add(new com.focusnode.model.LanActivity("You created the room", "#10B981", ""));
        server = new LanSessionServer(this);
        server.start(room.getPort());
    }

    public void joinRoom(LanRoom room, LanMember localMember) {
        this.localMember = localMember;
        client = new LanSessionClient(this);
        client.connect(room.getHostIp(), room.getPort(), localMember);
    }

    public void leaveRoom(LanMember localMember) {
        if (server != null) {
            server.stop();
            server = null;
        }
        if (client != null) {
            SyncPacket leavePacket = new SyncPacket("LEAVE", localMember);
            client.sendPacket(leavePacket);
            client.disconnect();
            client = null;
        }
        activeRoom.set(null);
        localMember = null;
        
        javafx.application.Platform.runLater(() -> {
            activeTransfers.clear();
            pomodoroState.set(null);
        });
    }



    public ObservableList<LanRoom> getDiscoveredRooms() {
        return discoveredRooms;
    }

    public ObjectProperty<LanRoom> activeRoomProperty() {
        return activeRoom;
    }

    public LanRoom getActiveRoom() {
        return activeRoom.get();
    }

    public ObservableList<LanTransfer> getActiveTransfers() {
        return activeTransfers;
    }

    public boolean isHostingRoom() {
        return server != null;
    }

    public LanMember getLocalMember() {
        return localMember;
    }

    public void publishLocalStatus(String status, long timeLeftSeconds) {
        if (localMember == null) {
            return;
        }

        localMember.setStatus(status);
        localMember.setTimeLeftSeconds(timeLeftSeconds);

        SyncPacket packet = new SyncPacket("STATUS_UPDATE", localMember);
        if (server != null) {
            server.broadcastPacket(packet);
        } else if (client != null) {
            client.sendPacket(packet);
        }
    }

    public void broadcastFileTransfer(com.focusnode.model.SyncPacket packet) {
        if (server != null) {
            server.broadcastPacket(packet);
        } else if (client != null) {
            client.sendPacket(packet);
        }
    }

    public long nextPomodoroSequence() {
        return ++pomodoroSequence;
    }

    public ObjectProperty<PomodoroSyncState> pomodoroStateProperty() {
        return pomodoroState;
    }

    public PomodoroSyncState getPomodoroState() {
        return pomodoroState.get();
    }

    public long getHostClockOffsetMillis() {
        return hostClockOffsetMillis == null ? 0 : hostClockOffsetMillis;
    }

    public void updateHostClockOffset(long clientSentAtEpochMillis, long hostSentAtEpochMillis, long clientReceivedAtEpochMillis) {
        long roundTripMillis = Math.max(0, clientReceivedAtEpochMillis - clientSentAtEpochMillis);
        long estimatedHostNowAtReceive = hostSentAtEpochMillis + roundTripMillis / 2;
        long measuredOffset = estimatedHostNowAtReceive - clientReceivedAtEpochMillis;

        if (hostClockOffsetMillis == null) {
            hostClockOffsetMillis = measuredOffset;
        } else {
            hostClockOffsetMillis = Math.round(hostClockOffsetMillis * 0.8 + measuredOffset * 0.2);
        }
    }

    public void publishPomodoroState(PomodoroSyncState state) {
        applyPomodoroState(state);
        if (server != null) {
            server.broadcastPacket(new SyncPacket("POMODORO_SYNC", state));
        }
    }

    public void applyPomodoroState(PomodoroSyncState state) {
        if (state == null) {
            return;
        }

        PomodoroSyncState currentState = pomodoroState.get();
        if (currentState == null || state.getSequence() >= currentState.getSequence()) {
            pomodoroSequence = Math.max(pomodoroSequence, state.getSequence());
            pomodoroState.set(state);
        }
    }

    public void updateDiscoveredRoom(com.focusnode.model.DiscoveryPacket dp) {
        // Check if room already exists
        for (int i = 0; i < discoveredRooms.size(); i++) {
            LanRoom existing = discoveredRooms.get(i);
            if (existing.getId().equals(dp.getRoomId())) {
                existing.setName(dp.getRoomName());
                existing.setMemberCount(dp.getMemberCount());
                existing.setMaxMembers(dp.getMaxMembers());
                return;
            }
        }
        // Room not found, add it
        LanRoom newRoom = new LanRoom(dp.getRoomId(), dp.getRoomName(), dp.getHostIp(), dp.getPort());
        newRoom.setMemberCount(dp.getMemberCount());
        newRoom.setMaxMembers(dp.getMaxMembers());
        discoveredRooms.add(newRoom);
    }
}
