package com.korotovsky.pusherd;

import com.korotovsky.pusherd.client.*;
import com.korotovsky.pusherd.network.ClientSocket;

import java.util.Map;
import java.util.logging.Logger;

public class Main implements Runnable {
    /**
     * @param args String[]
     */
    public static void main(String[] args) {
        new Thread(new Main()).start();
    }

    public void run() {
        GameServer gameServer = new GameServer(Logger.getLogger("pusherd"));

        while (gameServer.isAlive()) {
            try {
                gameServer.getLogger().info("-------------------");
                gameServer.getLogger().info("Connected clients: ");

                for (Map.Entry<ClientSocket, Player> entry : gameServer.getPlayers().entrySet()) {
                    Player player = entry.getValue();

                    gameServer.getLogger().info("   * Name: " + player.getName());
                }

                gameServer.getLogger().info("-------------------");

                Thread.sleep(10000);
            } catch (InterruptedException e) {
                gameServer.getLogger().warning(e.getMessage());
            }
        }
    }
}
