package com.korotovsky.server.evets;

import com.korotovsky.server.network.ClientSocket;

import java.io.IOException;

public interface GameEvents {
    public void onGetPlayers(ClientSocket clientSocket) throws IOException;

    public void onPlayerIsReady(ClientSocket clientSocket) throws IOException;

    public void onPush(ClientSocket clientSocket) throws IOException;
}
