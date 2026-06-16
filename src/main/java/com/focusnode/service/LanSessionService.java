package com.focusnode.service;

import com.focusnode.model.LanMember;
import com.focusnode.model.LanRoom;
import com.focusnode.model.LanTransfer;
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

    private LanSessionServer server;
    private LanSessionClient client;

    public LanSessionService() {
        // No mock data loaded by default
    }

    public void hostRoom(LanRoom room) {
        activeRoom.set(room);
        server = new LanSessionServer(this);
        server.start(room.getPort());
    }

    public void joinRoom(LanRoom room, LanMember localMember) {
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
