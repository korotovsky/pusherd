package com.korotovsky.pusherd.events;

import com.korotovsky.pusherd.client.Player;
import com.korotovsky.pusherd.network.ClientSocket;

public interface GameServerEvents {
    public void onPutClient(ClientSocket clientSocket, Player player);

    public void onRemoveClient(ClientSocket clientSocket);
}
