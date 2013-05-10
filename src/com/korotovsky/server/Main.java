package com.korotovsky.server;

import java.util.logging.Logger;

public class Main implements Runnable {
    /**
     * @param args String[]
     */
    public static void main(String[] args) {
        new Thread(new Main()).start();
    }

    public void run() {
        Bootstrap bootstrap = new Bootstrap(Logger.getLogger("server"));

        //bootstrap.getExecutorService().shutdown();
        while (!bootstrap.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                bootstrap.getLogger().warning(e.getMessage());
            }
        }
    }
}
