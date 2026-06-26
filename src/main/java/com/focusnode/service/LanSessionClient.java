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
import java.util.UUID;

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
                sendClockSyncPings();

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

    private void sendClockSyncPings() {
        ServiceLocator.getAsyncExecutor().submit(() -> {
            for (int i = 0; i < 5 && isRunning; i++) {
                SyncPacket ping = new SyncPacket();
                ping.setType("CLOCK_PING");
                ping.setPingId(UUID.randomUUID().toString());
                ping.setClientSentAtEpochMillis(System.currentTimeMillis());
                sendPacket(ping);
                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
    }

    private void handlePacket(SyncPacket packet) {
        if ("CLOCK_PONG".equals(packet.getType())) {
            sessionService.updateHostClockOffset(
                    packet.getClientSentAtEpochMillis(),
                    packet.getHostSentAtEpochMillis(),
                    System.currentTimeMillis()
            );
            return;
        }

        Platform.runLater(() -> {
            LanRoom room = sessionService.getActiveRoom();

            if (packet.getType().equals("STATE_SYNC")) {
                // Initial sync from server
                sessionService.activeRoomProperty().set(packet.getRoomState().toLanRoom());
                return;
            }
            if (packet.getType().equals("POMODORO_SYNC")) {
                sessionService.applyPomodoroState(packet.getPomodoroState());
                return;
            }

            if (room == null) return;

            switch (packet.getType()) {
                case "JOIN":
                    // Check if member already exists
                    boolean exists = room.getMembers().stream()
                            .anyMatch(m -> m.getId().equals(packet.getMember().id));
                    if (!exists) {
                        room.getMembers().add(packet.getMember().toLanMember());
                        room.setMemberCount(room.getMembers().size());
                        room.getActivities().add(new com.focusnode.model.LanActivity(packet.getMember().name + " joined", "#94A3B8", ""));
                    }
                    break;
                case "LEAVE":
                    room.getMembers().removeIf(m -> m.getId().equals(packet.getMember().id));
                    room.setMemberCount(room.getMembers().size());
                    room.getActivities().add(new com.focusnode.model.LanActivity(packet.getMember().name + " left", "#94A3B8", ""));
                    break;
                case "STATUS_UPDATE":
                    for (LanMember m : room.getMembers()) {
                        if (m.getId().equals(packet.getMember().id)) {
                            m.setStatus(packet.getMember().status);
                            m.setTimeLeftSeconds(packet.getMember().timeLeftSeconds);
                            m.setAudioMuted(packet.getMember().isAudioMuted);
                            break;
                        }
                    }
                    break;
                case "FILE_TRANSFER":
                    // A file is being sent, let's receive it
                    ServiceLocator.getLanFileTransferService().receiveFile(
                            socket.getInetAddress().getHostAddress(),
                            packet.getFileServerPort(),
                            packet.getFileName(),
                            packet.getFileSize(),
                            packet.getTransferId()
                    );
                    break;
                case "CRDT_SYNC":
                    com.focusnode.model.CrdtNote crdtNote = packet.getCrdtNote();
                    if (crdtNote != null) {
                        com.focusnode.repository.NoteRepository noteRepo = new com.focusnode.repository.NoteRepository();
                        com.focusnode.model.Note localNote = noteRepo.getNoteById(crdtNote.getNoteId());
                        if (localNote != null) {
                            localNote.setContent(crdtNote.getContent());
                            localNote.setUpdatedAt(java.time.LocalDateTime.now());
                            noteRepo.update(localNote);
                            
                            // Re-fetch in UI if NoteEditor is open
                            // To keep it simple, we just update the DB. The UI would poll or use a bus.
                        }
                    }
                    break;
            }
        });
    }
}
