package com.korotovsky.server.network.protocol.responses.system;

import com.korotovsky.server.network.protocol.*;

import java.io.BufferedWriter;

public class MessageResponse extends Response {
    protected String type = "message";
    protected String status = "ok";
    protected String message = "Unknown message";

    public MessageResponse(BufferedWriter writer) {
        super(writer);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public MessageResponse setMessage(String message) {
        this.message = message;

        return this;
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
