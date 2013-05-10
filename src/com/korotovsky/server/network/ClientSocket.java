package com.korotovsky.server.network;

import com.korotovsky.server.Callback;
import com.korotovsky.server.client.*;
import com.korotovsky.server.core.PlayerGame;
import com.korotovsky.server.network.protocol.*;
import com.korotovsky.server.network.protocol.responses.AcceptedResponse;
import com.korotovsky.server.network.protocol.responses.ErrorResponse;
import com.korotovsky.server.network.protocol.responses.MessageResponse;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Logger;

public class ClientSocket extends Thread {
    public final static Integer SUPPORTED_CLIENT_VERSION = 1;

    public final static String CALLBACK_BT_PUSH_CLIENT = "registerClient";
    public final static String CALLBACK_BT_POP_CLIENT = "unRegisterClient";
    public final static String CALLBACK_GAME_GET_PLAYERS = "getPlayers";
    public final static String CALLBACK_GAME_PLAYER_READY = "setPlayerReady";
    public final static String CALLBACK_GAME_PLAYER_PUSH = "setPlayerPush";

    public final static String CALLBACK_CLOSE_CONNECTION = "close";
    public final static String CALLBACK_MESSAGE = "message";
    public final static String CALLBACK_HANDSHAKE = "handshake";

    private Boolean isRegistered = false;

    private Logger logger;
    private Socket socket;

    private HashMap<String, Callback> callbacks;

    private BufferedWriter writer;
    private BufferedReader reader;

    public ClientSocket(Logger logger) {
        this.logger = logger;
        callbacks = new HashMap<String, Callback>();

        attach(ClientSocket.CALLBACK_MESSAGE, new Callback(this, CALLBACK_MESSAGE));
        attach(ClientSocket.CALLBACK_HANDSHAKE, new Callback(this, CALLBACK_HANDSHAKE));
        attach(ClientSocket.CALLBACK_CLOSE_CONNECTION, new Callback(this, CALLBACK_CLOSE_CONNECTION));
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

    public BufferedWriter getWriter() {
        return writer;
    }

    public ClientSocket setSocket(Socket socket) {
        this.socket = socket;

        return this;
    }

    public Boolean getIsRegistered() {
        return isRegistered;
    }

    private boolean read() throws Throwable {
        Request request = new Request(this, reader);

        String callback = request.dispatch();

        callbacks.get(callback).invoke(this, request);

        return true;
    }

    /**
     * Callback for simple echo message
     * @param clientSocket ClientSocket
     * @param request Request
     * @throws IOException
     */
    public void message(ClientSocket clientSocket, Request request) throws IOException {
        new MessageResponse(writer).setMessage(request.getLine()).send();
    }

    /**
     * Callback for close all readers and writers
     * @param clientSocket ClientSocket
     * @param request Request
     * @throws Throwable
     */
    public void close(ClientSocket clientSocket, Request request) throws Throwable {
        callbacks.get(CALLBACK_BT_POP_CLIENT).invoke(getId());

        writer.close();
        reader.close();
        socket.close();
    }

    /**
     * Callback for handshake request: helo:PROTOCOL:COMPUTER_NAME
     * @param clientSocket ClientSocket
     * @param request Request
     * @throws IOException
     */
    public void handshake(ClientSocket clientSocket, Request request) throws IOException {
        String[] parts = request.getLine().split(":");

        if (parts.length < 2) {
            new ErrorResponse(writer).setMessage(ErrorResponse.MSG_INVALID_HELO).send();
        }

        Integer version = Integer.parseInt(parts[1]);
        String name = parts[2];

        if (version.equals(SUPPORTED_CLIENT_VERSION)) {
            Info clientInfo = new Info(new PlayerGame());
            clientInfo.setVersion(version);
            clientInfo.setName(name);

            try {
                callbacks.get(CALLBACK_BT_PUSH_CLIENT).invoke(this.getId(), clientInfo);

                new AcceptedResponse(writer).send();

                isRegistered = true;
            } catch (Exception e) {
                logger.warning(e.getMessage());
            }
        } else {
            new ErrorResponse(writer).setMessage(ErrorResponse.MSG_MISMATCH_VERSION).send();
        }
    }
}