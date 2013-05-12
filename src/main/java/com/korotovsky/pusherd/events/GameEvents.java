package com.korotovsky.pusherd.events;

import com.korotovsky.pusherd.client.Player;
import com.korotovsky.pusherd.network.ClientSocket;

import java.io.IOException;

public interface GameEvents {
    public void onGetPlayers(ClientSocket clientSocket) throws IOException;

    public void onPlayerIsReady(ClientSocket clientSocket) throws IOException;

    public void onPush(ClientSocket clientSocket) throws IOException;

    public void onPlayerDisconnected(ClientSocket clientSocket, Player player) throws IOException;

    public void onPlayerConnected(ClientSocket clientSocket, Player player) throws IOException;
}
