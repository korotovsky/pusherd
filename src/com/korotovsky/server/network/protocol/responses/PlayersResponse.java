package com.korotovsky.server.network.protocol.responses;

import com.korotovsky.server.network.protocol.*;
import com.korotovsky.server.client.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PlayersResponse extends Response {
    protected String type = "game";
    protected String status = "ok";
    protected String message = "";

    private HashMap<Long, Info> players;

    public PlayersResponse(HashMap<Long, Info> players, BufferedWriter writer) {
        super(writer);

        this.players = players;
    }

    public void send() throws IOException {
        Integer counter = 0;
        for (Map.Entry<Long, Info> entry : players.entrySet()) {
            Info clientInfo = entry.getValue();

            if (counter != 0) {
                message += ",";
            }

            message += "name=" + clientInfo.getName();
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
