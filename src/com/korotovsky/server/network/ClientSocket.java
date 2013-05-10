package com.korotovsky.server.network;

import com.korotovsky.server.Callback;
import com.korotovsky.server.client.*;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class ClientSocket extends Thread {
    private final static String EXIT_SIGNAL = "exit";
    private final static String HELO_SIGNAL = "helo";
    private final static Integer SUPPORTED_CLIENT_VERSION = 1;

    private Boolean isRegistered = false;

    private Logger logger;
    private Socket socket;

    private HashMap<String, Callback> callbacks;

    public final static String CALLBACK_REGISTER_CLIENT = "registerClient";
    public final static String CALLBACK_UN_REGISTER_CLIENT = "unRegisterClient";

    private BufferedWriter writer;
    private BufferedReader reader;

    public ClientSocket(Logger logger) {
        this.logger = logger;
        callbacks = new HashMap<String, Callback>();
    }

    public ClientSocket attach(String callback, Callback callable) {
        callbacks.put(callback, callable);

        return this;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            logger.info(e.getMessage());
        }

        while (true) {
            try {
                read();
            } catch (Throwable e) {
                logger.info(e.getMessage());
                break;
            }
        }
    }

    public void write(String data) throws IOException {
        logger.info("Write to client: " + data);

        writer.write(data);
        writer.newLine();
        writer.flush();
    }

    public boolean read() throws Throwable {
        String line = reader.readLine();

        if (line == null) {
            return close();
        }

        logger.info("Remote socket address: " + socket.getRemoteSocketAddress().toString());
        logger.info("Received data: " + line);

        if (line.startsWith(HELO_SIGNAL)) {
            logger.info("Received helo");
            String[] heloParts = line.split(":");

            return registerClient(heloParts);
        } else if (line.equals(EXIT_SIGNAL)) {
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

    public boolean close() throws Throwable {
        logger.info("Remote socket has gone away: " + socket.getRemoteSocketAddress().toString());

        callbacks.get(CALLBACK_UN_REGISTER_CLIENT).invoke(this.getId());

        writer.close();
        reader.close();
        socket.close();

        return true;
    }

    private boolean registerClient(String[] parts) throws IOException{
        if (parts.length < 2) {
            write("Received invalid helo string");
            return false;
        }

        Integer version = Integer.parseInt(parts[1]);
        String name = parts[2];

        if (version.equals(SUPPORTED_CLIENT_VERSION)) {
            Info clientInfo = new Info();
            clientInfo.setVersion(version);
            clientInfo.setName("Client#" + name);

            try {
                callbacks.get(CALLBACK_REGISTER_CLIENT).invoke(this.getId(), clientInfo);

                write("Register success");

                return isRegistered = true;
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        } else {
            write("Mismatch client version, supported version is: " + SUPPORTED_CLIENT_VERSION);

            return false;
        }

        return false;
    }
}