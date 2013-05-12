package com.korotovsky.server.network.protocol;

import java.io.BufferedWriter;
import java.io.IOException;

public class Response {
    protected String type = null;
    protected String status = null;
    protected String message = null;

    protected BufferedWriter writer;

    public Response(BufferedWriter writer) {
        this.writer = writer;
    }

    public void send() throws IOException {
        writer.write("");
        writer.write(getType());
        writer.write(":");
        writer.write(getStatus());
        writer.write(":");
        writer.write(getMessage());
        writer.newLine();
        writer.flush();
    }

    public String getStatus() {
        return status;
    }

    public String getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Response setMessage(String message) {
        this.message = message;

        return this;
    }
}
