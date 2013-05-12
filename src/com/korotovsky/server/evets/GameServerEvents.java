package com.korotovsky.server.evets;

import com.korotovsky.server.client.Player;
import com.korotovsky.server.network.ClientSocket;

public interface GameServerEvents {
    public void onPutClient(ClientSocket clientSocket, Player player);

    public void onRemoveClient(ClientSocket clientSocket);
}
