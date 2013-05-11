package com.korotovsky.server;

import com.korotovsky.server.client.*;

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
        GameServer gameServer = new GameServer(Logger.getLogger("server"));

        while (gameServer.isAlive()) {
            try {
                gameServer.getLogger().info("-------------------");
                gameServer.getLogger().info("Connected clients: ");

                for (Map.Entry<Integer, Info> entry : gameServer.getClients().entrySet()) {
                    Info clientInfo = entry.getValue();

                    gameServer.getLogger().info("   * Name: " + clientInfo.getName());
                }

                gameServer.getLogger().info("-------------------");

                Thread.sleep(10000);
            } catch (InterruptedException e) {
                gameServer.getLogger().warning(e.getMessage());
            }
        }
    }
}
