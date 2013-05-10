package com.korotovsky.server.network.protocol.responses;

import com.korotovsky.server.network.ClientSocket;
import com.korotovsky.server.network.protocol.*;

import java.io.BufferedWriter;

public class ErrorResponse extends Response {
    public final static String MSG_MISMATCH_VERSION = "Mismatch client version, supported version is: " + ClientSocket.SUPPORTED_CLIENT_VERSION;
    public final static String MSG_INVALID_HELO = "Received invalid helo string";

    protected String type = "message";
    protected String status = "error";
    protected String message = "Unknown error";

    public ErrorResponse(BufferedWriter writer) {
        super(writer);
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public ErrorResponse setMessage(String message) {
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
