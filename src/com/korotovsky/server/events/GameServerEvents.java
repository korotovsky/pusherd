package com.korotovsky.server.events;

import com.korotovsky.server.client.Player;
import com.korotovsky.server.network.ClientSocket;

public interface GameServerEvents {
    public void onPutClient(ClientSocket clientSocket, Player player);

    public void onRemoveClient(ClientSocket clientSocket);
}
