package com.focusnode.controller;

import com.focusnode.model.LanRoom;
import com.focusnode.model.LanMember;
import com.focusnode.service.LanSessionService;
import com.focusnode.service.ServiceLocator;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.UUID;

public class LanRoomActionsController implements Initializable {

    @FXML private HBox createRoomBtn;
    @FXML private HBox joinRoomBtn;

    private final LanSessionService sessionService = ServiceLocator.getLanSessionService();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Simple mock actions for now
        createRoomBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            LanRoom room = new LanRoom(
                UUID.randomUUID().toString(),
                "My New Focus Room",
                "127.0.0.1",
                5050
            );
            LanMember host = new LanMember("u1", "You", "127.0.0.1", true);
            room.getMembers().add(host);
            
            sessionService.activeRoomProperty().set(room);
            
            // Start broadcasting presence
            ServiceLocator.getLanDiscoveryService().startBroadcasting(room);
        });

        joinRoomBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            System.out.println("Join Room clicked - to be implemented");
        });
    }
}
