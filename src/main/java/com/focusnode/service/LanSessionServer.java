package com.focusnode.service;

import com.focusnode.model.LanMember;
import com.focusnode.model.LanRoom;
import com.focusnode.model.SyncPacket;
import com.google.gson.Gson;
import javafx.application.Platform;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class LanSessionServer {

    private final LanSessionService sessionService;
    private final Gson gson = new Gson();
    private ServerSocket serverSocket;
    private final List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private volatile boolean isRunning = false;

    public LanSessionServer(LanSessionService sessionService) {
        this.sessionService = sessionService;
    }

    public void start(int port) {
        isRunning = true;
        ServiceLocator.getAsyncExecutor().submit(() -> {
            try {
                serverSocket = new ServerSocket(port);
                while (isRunning) {
                    Socket clientSocket = serverSocket.accept();
                    ClientHandler handler = new ClientHandler(clientSocket);
                    clients.add(handler);
                    ServiceLocator.getAsyncExecutor().submit(handler);
                }
            } catch (Exception e) {
                if (isRunning) e.printStackTrace();
            }
        });
    }

    public void stop() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            for (ClientHandler client : clients) {
                client.close();
            }
            clients.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void broadcastPacket(SyncPacket packet) {
        String json = gson.toJson(packet);
        for (ClientHandler client : clients) {
            client.sendMessage(json);
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private com.focusnode.model.LanMemberDto clientMember;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Initial sync: Send current room state to new client
                LanRoom activeRoom = sessionService.getActiveRoom();
                if (activeRoom != null) {
                    SyncPacket initialSync = new SyncPacket("STATE_SYNC", activeRoom);
                    sendMessage(gson.toJson(initialSync));
                }
                if (sessionService.getPomodoroState() != null) {
                    SyncPacket timerSync = new SyncPacket("POMODORO_SYNC", sessionService.getPomodoroState());
                    sendMessage(gson.toJson(timerSync));
                }

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Handle incoming packets from this client
                    SyncPacket packet = gson.fromJson(inputLine, SyncPacket.class);
                    if ("JOIN".equals(packet.getType()) && packet.getMember() != null) {
                        this.clientMember = packet.getMember();
                    }
                    handlePacket(packet, this);
                }
            } catch (Exception e) {
                // Client disconnected ungracefully
            } finally {
                close();
                clients.remove(this);
                // Handle ungraceful disconnect by simulating a LEAVE packet if they joined
                if (clientMember != null) {
                    SyncPacket leavePacket = new SyncPacket("LEAVE", clientMember.toLanMember());
                    Platform.runLater(() -> {
                        LanRoom room = sessionService.getActiveRoom();
                        if (room != null) {
                            room.getMembers().removeIf(m -> m.getId().equals(clientMember.id));
                            room.setMemberCount(room.getMembers().size());
                            room.getActivities().add(new com.focusnode.model.LanActivity(clientMember.name + " disconnected", "#EF4444", ""));
                            broadcastPacket(leavePacket);
                        }
                    });
                }
            }
        }

        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        public void close() {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ignored) {}
        }
    }

    private void handlePacket(SyncPacket packet, ClientHandler sender) {
        if ("CLOCK_PING".equals(packet.getType())) {
            SyncPacket pong = new SyncPacket();
            pong.setType("CLOCK_PONG");
            pong.setPingId(packet.getPingId());
            pong.setClientSentAtEpochMillis(packet.getClientSentAtEpochMillis());
            pong.setHostSentAtEpochMillis(System.currentTimeMillis());
            sender.sendMessage(gson.toJson(pong));
            return;
        }

        // Run updates on JavaFX thread
        Platform.runLater(() -> {
            LanRoom room = sessionService.getActiveRoom();
            if (room == null) return;

            switch (packet.getType()) {
                case "JOIN":
                    // Avoid duplicates if reconnecting
                    room.getMembers().removeIf(m -> m.getId().equals(packet.getMember().id));
                    room.getMembers().add(packet.getMember().toLanMember());
                    room.setMemberCount(room.getMembers().size());
                    room.getActivities().add(new com.focusnode.model.LanActivity(packet.getMember().name + " joined", "#94A3B8", ""));
                    // Broadcast the join to everyone else
                    broadcastPacket(packet);
                    break;
                case "LEAVE":
                    room.getMembers().removeIf(m -> m.getId().equals(packet.getMember().id));
                    room.setMemberCount(room.getMembers().size());
                    room.getActivities().add(new com.focusnode.model.LanActivity(packet.getMember().name + " left", "#94A3B8", ""));
                    broadcastPacket(packet);
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
                    // Re-broadcast to sync others
                    broadcastPacket(packet);
                    break;
                case "POMODORO_SYNC":
                    // Pomodoro timer is Host-authoritative; clients cannot overwrite it.
                    break;
                case "FILE_TRANSFER":
                    // Re-broadcast to all other clients
                    broadcastPacket(packet);
                    
                    // The server itself should also receive it
                    ServiceLocator.getLanFileTransferService().receiveFile(
                            sender.socket.getInetAddress().getHostAddress(),
                            packet.getFileServerPort(),
                            packet.getFileName(),
                            packet.getFileSize(),
                            packet.getTransferId()
                    );
                    break;
            }
        });
    }
}
