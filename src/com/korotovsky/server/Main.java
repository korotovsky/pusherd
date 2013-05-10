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
        Logger logger = Logger.getLogger("server");
        Bootstrap bootstrap = new Bootstrap(logger);

        while (bootstrap.isAlive()) {
            try {
                bootstrap.getLogger().info("-------------------");
                bootstrap.getLogger().info("Connected clients: ");

                for (Map.Entry<Long, Info> entry : bootstrap.getClients().entrySet()) {
                    Info clientInfo = entry.getValue();

                    bootstrap.getLogger().info("   * Name: " + clientInfo.getName());
                }

                bootstrap.getLogger().info("-------------------");

                Thread.sleep(10000);
            } catch (InterruptedException e) {
                bootstrap.getLogger().warning(e.getMessage());
            }
        }
    }
}
