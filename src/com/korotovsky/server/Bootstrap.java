package com.korotovsky.server;

import com.korotovsky.server.network.*;
import com.korotovsky.server.client.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class Bootstrap {
    private final Logger logger;
    private Integer port = 10001;
    private Integer connections = 1024;
    private String address = "127.0.0.1";
    private ExecutorService clientSockets;
    private HashMap<Long, Info> clients;
    private Thread thread;

    public Bootstrap(final Logger logger) {
        clientSockets = Executors.newCachedThreadPool();
        clients = new HashMap<Long, Info>();

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

    public HashMap<Long, Info> getClients() {
        return clients;
    }

    public Boolean isAlive() {
        return !thread.isInterrupted();
    }

    private void runClientSocketsThread(final ServerSocket serverSocket) {
        final Bootstrap that = this;

        thread = new Thread() {
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

                ClientSocket clientSocket = new ClientSocket(logger);

                clientSocket.setSocket(socket);
                clientSocket.attach(ClientSocket.CALLBACK_REGISTER_CLIENT, new Callback(that, "registerClientCallback"));
                clientSocket.attach(ClientSocket.CALLBACK_UN_REGISTER_CLIENT, new Callback(that, "unRegisterClientCallback"));

                synchronized (this) {
                    clientSockets.submit(clientSocket);
                }
            }
        };

        thread.start();
    }

    public void registerClientCallback(Long key, Info clientInfo)
    {
        synchronized (this) {
            clients.put(key, clientInfo);
        }
    }

    public void unRegisterClientCallback(Long key)
    {
        synchronized (this) {
            clients.remove(key);
        }
    }
}