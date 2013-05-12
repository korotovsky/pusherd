package com.korotovsky.server.network.protocol.responses.game;

import com.korotovsky.server.client.Player;
import com.korotovsky.server.network.protocol.Response;

import java.io.BufferedWriter;

public class GameWinnerResponse extends Response {
    protected String type = "game";
    protected String status = "ok";
    protected String message = "winner";
    protected Player player;

    public GameWinnerResponse(Player player, BufferedWriter writer) {
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
