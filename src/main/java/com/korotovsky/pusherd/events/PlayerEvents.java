package com.korotovsky.pusherd.events;

import com.korotovsky.pusherd.network.protocol.Request;

import java.io.IOException;

public interface PlayerEvents {
    public void onCloseConnection(Request request) throws Throwable;

    public void onEchoMessage(Request request) throws IOException;
}
