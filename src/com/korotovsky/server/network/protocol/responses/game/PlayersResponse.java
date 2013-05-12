package com.korotovsky.server.network.protocol.responses.game;

import com.korotovsky.server.network.ClientSocket;
import com.korotovsky.server.network.protocol.*;
import com.korotovsky.server.client.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayersResponse extends Response {
    protected String type = "game";
    protected String status = "ok";
    protected String message = "";

    private ConcurrentHashMap<ClientSocket, Player> players;

    public PlayersResponse(ConcurrentHashMap<ClientSocket, Player> players, BufferedWriter writer) {
        super(writer);

        this.players = players;
    }

    public void send() throws IOException {
        Integer counter = 0;
        for (Map.Entry<ClientSocket, Player> entry : players.entrySet()) {
            Player player = entry.getValue();

            if (counter != 0) {
                message += ",";
            }

            message += "name=" + player.getName();
            counter++;
        }

        super.send();
    }

    @Override
    public String getMessage() {
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
