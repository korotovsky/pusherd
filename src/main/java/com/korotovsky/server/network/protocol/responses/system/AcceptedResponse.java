package com.korotovsky.server.network.protocol.responses.system;

import com.korotovsky.server.network.protocol.*;

import java.io.BufferedWriter;

public class AcceptedResponse extends Response {
    protected String type = "message";
    protected String status = "ok";
    protected String message = "Successful handshake";

    public AcceptedResponse(BufferedWriter writer) {
        super(writer);
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
