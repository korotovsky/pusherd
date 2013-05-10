package com.korotovsky.server.network;

import com.korotovsky.server.Callback;
import com.korotovsky.server.client.*;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.logging.Logger;

public class ClientSocket extends Thread {
    private final static String EXIT_SIGNAL = "exit";
    private final static String HELO_SIGNAL = "helo";
    private final static Integer SUPPORTED_CLIENT_VERSION = 1;
    private Boolean isRegistered = false;
    private Logger logger;
    private Socket socket;
    private Callback registerCallback;
    private Callback unRegisterCallback;
    private PrintWriter writer;
    private BufferedReader reader;

    public ClientSocket(Logger logger)
    {
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            logger.info(e.getMessage());
        }

        while (true) {
            try {
                read();
            } catch (IOException e) {
                logger.info(e.getMessage());
                break;
            }
        }
    }

    public boolean read() throws IOException {
        String line = reader.readLine();

        if (line == null) {
            logger.info("Received nilled data");
            logger.info("Remote socket has gone away: " + socket.getRemoteSocketAddress().toString());
            return close();
        }

        logger.info("Remote socket address: " + socket.getRemoteSocketAddress().toString());
        logger.info("Received data: " + line);

        if (line.startsWith(HELO_SIGNAL)) {
            logger.info("Received helo");
            String[] heloParts = line.split(":");

            return registerClient(heloParts);
        } else if (line.equals(EXIT_SIGNAL)) {
            logger.info("Remote socket has gone away: " + socket.getRemoteSocketAddress().toString());
            return close();

        } else if (!isRegistered) {
            return close();
        }

        return true;
    }

    /**
     * @param socket Socket
     * @return ClientSocket
     */
    public ClientSocket setSocket(Socket socket) {
        this.socket = socket;

        return this;
    }

    /**
     * @param registerCallback Callback
     * @return ClientSocket
     */
    public ClientSocket setRegisterCallback(Callback registerCallback) {
        this.registerCallback = registerCallback;

        return this;
    }

    /**
     * @param unRegisterCallback Callback
     * @return ClientSocket
     */
    public ClientSocket setUnRegisterCallback(Callback unRegisterCallback) {
        this.unRegisterCallback = unRegisterCallback;

        return this;
    }

    public boolean close() throws IOException {
        writer.close();
        reader.close();
        socket.close();

        return true;
    }

    private boolean registerClient(String[] parts)
    {
        Integer version = Integer.parseInt(parts[1]);
        String name = parts[2];

        if (version.equals(SUPPORTED_CLIENT_VERSION)) {
            Info clientInfo = new Info();
            clientInfo.setVersion(version);
            clientInfo.setName("Client#" + name);

            try {
                registerCallback.invoke(this.getId(), clientInfo);
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }

            return isRegistered = true;
        }

        return false;
    }

    @Override
    protected void finalize() throws Throwable {
        unRegisterCallback.invoke(this.getId());

        super.finalize();
    }
}