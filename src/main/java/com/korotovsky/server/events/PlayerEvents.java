package com.korotovsky.server.events;

import com.korotovsky.server.network.protocol.Request;

import java.io.IOException;

public interface PlayerEvents {
    public void onCloseConnection(Request request) throws Throwable;

    public void onEchoMessage(Request request) throws IOException;
}
