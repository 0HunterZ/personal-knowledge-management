package com.focusnode.service;

import com.focusnode.model.LanMember;
import com.focusnode.model.LanRoom;
import com.focusnode.model.SyncPacket;
import com.google.gson.Gson;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LanSessionClient {

    private final LanSessionService sessionService;
    private final Gson gson = new Gson();
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private volatile boolean isRunning = false;

    public LanSessionClient(LanSessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void connect(String hostIp, int port, LanMember localMember) {
        isRunning = true;
        ServiceLocator.getAsyncExecutor().submit(() -> {
            try {
                socket = new Socket(hostIp, port);
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Send JOIN packet
                SyncPacket joinPacket = new SyncPacket("JOIN", localMember);
                sendPacket(joinPacket);

                String inputLine;
                while (isRunning && (inputLine = in.readLine()) != null) {
                    SyncPacket packet = gson.fromJson(inputLine, SyncPacket.class);
                    handlePacket(packet);
                }
            } catch (Exception e) {
                if (isRunning) e.printStackTrace();
            } finally {
                disconnect();
            }
        });
    }

    public void disconnect() {
        isRunning = false;
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (Exception ignored) {}
    }

    public void sendPacket(SyncPacket packet) {
        if (out != null) {
            out.println(gson.toJson(packet));
        }
    }

    private void handlePacket(SyncPacket packet) {
        Platform.runLater(() -> {
            LanRoom room = sessionService.getActiveRoom();

            if (packet.getType().equals("STATE_SYNC")) {
                // Initial sync from server
                sessionService.activeRoomProperty().set(packet.getRoomState());
                return;
            }

            if (room == null) return;

            switch (packet.getType()) {
                case "JOIN":
                    // Check if member already exists
                    boolean exists = room.getMembers().stream()
                            .anyMatch(m -> m.getId().equals(packet.getMember().getId()));
                    if (!exists) {
                        room.getMembers().add(packet.getMember());
                        room.setMemberCount(room.getMembers().size());
                    }
                    break;
                case "LEAVE":
                    room.getMembers().removeIf(m -> m.getId().equals(packet.getMember().getId()));
                    room.setMemberCount(room.getMembers().size());
                    break;
                case "STATUS_UPDATE":
                    for (LanMember m : room.getMembers()) {
                        if (m.getId().equals(packet.getMember().getId())) {
                            m.setStatus(packet.getMember().getStatus());
                            m.setTimeLeftSeconds(packet.getMember().getTimeLeftSeconds());
                            m.setAudioMuted(packet.getMember().isAudioMuted());
                            break;
                        }
                    }
                    break;
            }
        });
    }
}
