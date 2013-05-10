package com.korotovsky.server.network.protocol;

import com.korotovsky.server.network.ClientSocket;

import java.io.BufferedReader;

public class Request {
    private final static String EXIT_SIGNAL = "exit";
    private final static String HELO_SIGNAL = "helo";
    private final static String CLIENTS_SIGNAL = "clients";
    private final static String READY_SIGNAL = "ready";
    private final static String PUSH_SIGNAL = "push";

    private BufferedReader reader;
    private ClientSocket clientSocket;
    private String line;

    public Request(ClientSocket clientSocket, BufferedReader reader) {
        this.clientSocket = clientSocket;
        this.reader = reader;
    }

    public String dispatch() throws Throwable {
        line = reader.readLine();

        if (line == null) {
            return ClientSocket.CALLBACK_CLOSE_CONNECTION;
        }

        if (line.equals(EXIT_SIGNAL)) {
            return ClientSocket.CALLBACK_CLOSE_CONNECTION;

        } else if (line.startsWith(HELO_SIGNAL)) {
            return ClientSocket.CALLBACK_HANDSHAKE;

        } else if (!clientSocket.getIsRegistered()) {
            return ClientSocket.CALLBACK_CLOSE_CONNECTION;

        } else if (line.equals(CLIENTS_SIGNAL)) {
            return ClientSocket.CALLBACK_GAME_GET_PLAYERS;

        } else if (line.equals(READY_SIGNAL)) {
            return ClientSocket.CALLBACK_GAME_PLAYER_READY;

        } else if (line.equals(PUSH_SIGNAL)) {
            return ClientSocket.CALLBACK_GAME_PLAYER_PUSH;
        }

        return ClientSocket.CALLBACK_MESSAGE;
    }

    public String getLine() {
        return line;
    }
}
