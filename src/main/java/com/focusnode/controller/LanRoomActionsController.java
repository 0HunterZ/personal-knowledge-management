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

    private Runnable onCreateRoomCallback;
    private Runnable onJoinRoomCallback;

    public void setCallbacks(Runnable onCreate, Runnable onJoin) {
        this.onCreateRoomCallback = onCreate;
        this.onJoinRoomCallback = onJoin;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        createRoomBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (onCreateRoomCallback != null) onCreateRoomCallback.run();
        });

        joinRoomBtn.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (onJoinRoomCallback != null) onJoinRoomCallback.run();
        });
    }
}
