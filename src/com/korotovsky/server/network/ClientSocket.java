package com.korotovsky.server.network;

import com.korotovsky.server.Callback;
import com.korotovsky.server.client.*;
import com.korotovsky.server.network.protocol.responses.AcceptedResponse;
import com.korotovsky.server.network.protocol.responses.ErrorResponse;
import com.korotovsky.server.network.protocol.responses.MessageResponse;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class ClientSocket extends Thread {
    public final static Integer SUPPORTED_CLIENT_VERSION = 1;
    public final static String CALLBACK_REGISTER_CLIENT = "registerClient";
    public final static String CALLBACK_UN_REGISTER_CLIENT = "unRegisterClient";

    private final static String EXIT_SIGNAL = "exit";
    private final static String HELO_SIGNAL = "helo";

    private Boolean isRegistered = false;

    private Logger logger;
    private Socket socket;

    private HashMap<String, Callback> callbacks;

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

    public ClientSocket setSocket(Socket socket) {
        this.socket = socket;

        return this;
    }

    private boolean read() throws Throwable {
        String line = reader.readLine();

        if (line == null) {
            return close();
        }

        if (line.startsWith(HELO_SIGNAL)) {
            return registerClient(line);
        } else if (line.equals(EXIT_SIGNAL)) {
            return close();
        } else if (!isRegistered) {
            return close();
        }

        new MessageResponse(writer).setMessage(line).send();

        return true;
    }

    private boolean close() throws Throwable {
        callbacks.get(CALLBACK_UN_REGISTER_CLIENT).invoke(this.getId());

        writer.close();
        reader.close();
        socket.close();

        return true;
    }

    private boolean registerClient(String line) throws IOException {
        String[] parts = line.split(":");

        if (parts.length < 2) {
            new ErrorResponse(writer).setMessage(ErrorResponse.MSG_INVALID_HELO).send();

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

                new AcceptedResponse(writer).send();

                return isRegistered = true;
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        } else {
            new ErrorResponse(writer).setMessage(ErrorResponse.MSG_MISMATCH_VERSION).send();
        }

        return false;
    }
}