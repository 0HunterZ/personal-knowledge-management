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

                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    // Handle incoming packets from this client
                    SyncPacket packet = gson.fromJson(inputLine, SyncPacket.class);
                    handlePacket(packet, this);
                }
            } catch (Exception e) {
                // Client disconnected
            } finally {
                close();
                clients.remove(this);
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
        // Run updates on JavaFX thread
        Platform.runLater(() -> {
            LanRoom room = sessionService.getActiveRoom();
            if (room == null) return;

            switch (packet.getType()) {
                case "JOIN":
                    room.getMembers().add(packet.getMember());
                    room.setMemberCount(room.getMembers().size());
                    // Broadcast the join to everyone else
                    broadcastPacket(packet);
                    break;
                case "LEAVE":
                    room.getMembers().removeIf(m -> m.getId().equals(packet.getMember().getId()));
                    room.setMemberCount(room.getMembers().size());
                    broadcastPacket(packet);
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
                    // Re-broadcast to sync others
                    broadcastPacket(packet);
                    break;
            }
        });
    }
}
