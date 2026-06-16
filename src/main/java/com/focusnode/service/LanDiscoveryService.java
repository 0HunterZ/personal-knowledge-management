package com.focusnode.service;

import com.focusnode.model.DiscoveryPacket;
import com.focusnode.model.LanRoom;
import com.google.gson.Gson;
import javafx.application.Platform;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

public class LanDiscoveryService {

    private static final int DISCOVERY_PORT = 5055;
    private static final String MULTICAST_GROUP = "230.0.0.0";
    
    private final LanSessionService sessionService;
    private final ExecutorService executor;
    private final Gson gson = new Gson();

    private final AtomicBoolean isBroadcasting = new AtomicBoolean(false);
    private final AtomicBoolean isListening = new AtomicBoolean(false);

    private MulticastSocket listenerSocket;
    private DatagramSocket broadcastSocket;

    public LanDiscoveryService(LanSessionService sessionService, ExecutorService executor) {
        this.sessionService = sessionService;
        this.executor = executor;
    }

    public void startBroadcasting(LanRoom room) {
        if (isBroadcasting.get()) return;
        isBroadcasting.set(true);

        executor.submit(() -> {
            try {
                broadcastSocket = new DatagramSocket();
                broadcastSocket.setBroadcast(true);
                InetAddress group = InetAddress.getByName(MULTICAST_GROUP);

                while (isBroadcasting.get()) {
                    DiscoveryPacket packet = new DiscoveryPacket(room);
                    String json = gson.toJson(packet);
                    byte[] buffer = json.getBytes();

                    DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length, group, DISCOVERY_PORT);
                    broadcastSocket.send(datagramPacket);

                    Thread.sleep(2000); // Broadcast every 2 seconds
                }
            } catch (Exception e) {
                if (isBroadcasting.get()) {
                    e.printStackTrace();
                }
            } finally {
                if (broadcastSocket != null && !broadcastSocket.isClosed()) {
                    broadcastSocket.close();
                }
            }
        });
    }

    public void stopBroadcasting() {
        isBroadcasting.set(false);
        if (broadcastSocket != null && !broadcastSocket.isClosed()) {
            broadcastSocket.close();
        }
    }

    public void startListening() {
        if (isListening.get()) return;
        isListening.set(true);

        executor.submit(() -> {
            try {
                listenerSocket = new MulticastSocket(DISCOVERY_PORT);
                InetAddress group = InetAddress.getByName(MULTICAST_GROUP);
                listenerSocket.joinGroup(group);

                byte[] buffer = new byte[1024];

                while (isListening.get()) {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    listenerSocket.receive(packet);

                    String json = new String(packet.getData(), 0, packet.getLength());
                    DiscoveryPacket dp = gson.fromJson(json, DiscoveryPacket.class);

                    // Add or update room in session service
                    Platform.runLater(() -> sessionService.updateDiscoveredRoom(dp));
                }
            } catch (Exception e) {
                if (isListening.get()) {
                    e.printStackTrace();
                }
            } finally {
                if (listenerSocket != null && !listenerSocket.isClosed()) {
                    listenerSocket.close();
                }
            }
        });
    }

    public void stopListening() {
        isListening.set(false);
        if (listenerSocket != null && !listenerSocket.isClosed()) {
            try {
                listenerSocket.leaveGroup(InetAddress.getByName(MULTICAST_GROUP));
            } catch (IOException ignored) {}
            listenerSocket.close();
        }
    }
}
