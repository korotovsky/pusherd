package com.korotovsky.server;

import com.korotovsky.server.core.Game;
import com.korotovsky.server.client.*;
import com.korotovsky.server.network.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class GameServer {
    private final Logger logger;

    private Integer port = 10001;
    private Integer connections = 1024;
    private String address = "127.0.0.1";

    private ExecutorService workers;
    private ExecutorService listener;

    private HashMap<Integer, ClientSocket> clientSockets;
    private HashMap<Integer, Info> clients;

    private Runnable runnable;
    private Game game;

    public GameServer(final Logger logger) {
        workers = Executors.newCachedThreadPool();
        listener = Executors.newSingleThreadExecutor();

        clientSockets = new HashMap<Integer, ClientSocket>();
        clients = new HashMap<Integer, Info>();

        game = new Game(this);

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

    public HashMap<Integer, Info> getClients() {
        return clients;
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
                Socket socket = serverSocket.accept();

                logger.info("Client connected");

                ClientSocket clientSocket = new ClientSocket(socket, logger);

                clientSocket.attach(ClientSocket.CALLBACK_BT_PUT_CLIENT, new TFunction2<ClientSocket, Info, TFunction>() {
                    @Override
                    public synchronized TFunction invoke2(ClientSocket clientSocket, Info clientInfo) {
                        clients.put(clientSocket.hashCode(), clientInfo);
                    }
                });
                clientSocket.attach(ClientSocket.CALLBACK_BT_POP_CLIENT, new Callback1<ClientSocket>() {
                    @Override
                    public synchronized void invoke(ClientSocket clientSocket) {
                        clientSockets.remove(clientSocket.hashCode());
                        clients.remove(clientSocket.hashCode());
                    }
                });
                clientSocket.attach(ClientSocket.CALLBACK_GAME_GET_PLAYERS, new Callback(that.game, ClientSocket.CALLBACK_GAME_GET_PLAYERS));
                clientSocket.attach(ClientSocket.CALLBACK_GAME_PLAYER_PUSH, new Callback(that.game, ClientSocket.CALLBACK_GAME_PLAYER_PUSH));
                clientSocket.attach(ClientSocket.CALLBACK_GAME_PLAYER_READY, new Callback(that.game, ClientSocket.CALLBACK_GAME_PLAYER_READY));

                synchronized (this) {
                    clientSockets.put(clientSocket.hashCode(), clientSocket);
                    workers.submit(clientSocket);
                }
            }
        });
    }
}