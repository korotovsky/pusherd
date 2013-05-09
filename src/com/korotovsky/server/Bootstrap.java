package com.korotovsky.server;

import com.korotovsky.server.network.ClientSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

class Bootstrap {
    private final Logger logger;
    private Integer port = 10000;
    private Integer connections = 1024;
    private String address = "127.0.0.1";
    private ExecutorService clientSockets;
    private Thread thread;

    public Bootstrap(final Logger logger) {
        clientSockets = Executors.newCachedThreadPool();
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

    /**
     * @return Logger
     */
    public Logger getLogger()
    {
        return logger;
    }

    /**
     * @return ExecutorService
     */
    public ExecutorService getExecutorService() {
        return clientSockets;
    }

    /**
     * @return Boolean
     */
    public Boolean isTerminated()
    {
        return thread.isAlive();
    }

    /**
     * @param serverSocket ServerSocket
     */
    protected void runClientSocketsThread(final ServerSocket serverSocket) {
        thread = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        logger.info("Waiting for a client...");

                        addClientSockets(serverSocket);
                    } catch (IOException e) {
                        logger.warning(e.getMessage());
                        break;
                    }
                }
            }

            public synchronized void addClientSockets(final ServerSocket serverSocket) throws IOException
            {
                Socket socket = serverSocket.accept();

                logger.info("Client connected");

                ClientSocket clientSocket = new ClientSocket(logger);
                clientSocket.setSocket(socket);

                clientSockets.submit(clientSocket);
            }
        };

        thread.start();
    }
}