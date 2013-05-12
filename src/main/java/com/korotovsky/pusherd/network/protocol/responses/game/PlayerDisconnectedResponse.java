package com.korotovsky.pusherd.network.protocol.responses.game;

import com.korotovsky.pusherd.client.Player;
import com.korotovsky.pusherd.network.protocol.Response;

import java.io.BufferedWriter;

public class PlayerDisconnectedResponse extends Response {
    protected String type = "game";
    protected String status = "ok";
    protected String message = "playerDisconnected";
    protected Player player;

    public PlayerDisconnectedResponse(Player player, BufferedWriter writer) {
        super(writer);

        this.player = player;
    }

    @Override
    public String getMessage() {
        message += ":" + player.getName();

        return message;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getStatus() {
        return status;
    }
}
