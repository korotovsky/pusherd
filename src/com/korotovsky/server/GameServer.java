package com.korotovsky.server;

import com.korotovsky.server.core.Game;
import com.korotovsky.server.client.*;
import com.korotovsky.server.events.GameServerEvents;
import com.korotovsky.server.network.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class GameServer implements GameServerEvents {
    private final Logger logger;

    private Integer port = 10001;
    private Integer connections = 1024;
    private String address = "127.0.0.1";

    private ExecutorService workers = Executors.newCachedThreadPool();
    private ExecutorService listener = Executors.newSingleThreadExecutor();

    private Set<ClientSocket> clientSockets = Collections.synchronizedSet(new HashSet<ClientSocket>());
    private ConcurrentHashMap<ClientSocket, Player> clients = new ConcurrentHashMap<ClientSocket, Player>();

    private Game game = new Game(this);

    public GameServer(final Logger logger) {
        this.logger = logger;

        try {
            InetAddress inetAddress = InetAddress.getByName(address);

            try {
                this.runClientSocketsThread(new ServerSocket(port, connections, inetAddress));
            } catch (IOException e) {
                logger.warning(e.getMessage());
            }
        } catch (UnknownHostException e) {
            logger.warning(e.getMessage());
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public Game getGame() {
        return game;
    }

    public ConcurrentHashMap<ClientSocket, Player> getPlayers() {
        return clients;
    }

    public Integer getPlayersCount() {
        return clients.size();
    }

    public ExecutorService getWorkers() {
        return workers;
    }

    public Boolean isAlive() {
        return !listener.isTerminated();
    }

    private void runClientSocketsThread(final ServerSocket serverSocket) {
        final GameServer that = this;

        listener.submit(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        logger.info("Waiting for a client...");

                        addClientSockets(serverSocket);
                    } catch (IOException e) {
                        logger.warning(e.getMessage());

                        Thread.currentThread().interrupt();

                        break;
                    }
                }
            }

            private void addClientSockets(final ServerSocket serverSocket) throws IOException {
                ClientSocket clientSocket = new ClientSocket(that, serverSocket.accept());

                clientSockets.add(clientSocket);

                synchronized (this) {
                    workers.submit(clientSocket);
                }
            }
        });
    }

    public void onPutClient(ClientSocket clientSocket, Player player) {
        clients.put(clientSocket, player);
    }

    public void onRemoveClient(ClientSocket clientSocket) {
        clientSockets.remove(clientSocket);
        clients.remove(clientSocket);
    }
}