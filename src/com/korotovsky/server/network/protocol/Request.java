package com.korotovsky.server.network.protocol;

import com.korotovsky.server.core.Game;
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
    private Game game;
    private String line;

    public Request(Game game, BufferedReader reader, ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
        this.reader = reader;
        this.game = game;
    }

    public void dispatch() throws Throwable {
        line = reader.readLine();

        if (line == null) {
            clientSocket.onCloseConnection(this);
            return;
        }

        if (line.equals(EXIT_SIGNAL)) {
            clientSocket.onCloseConnection(this);
            return;

        } else if (line.startsWith(HELO_SIGNAL)) {
            clientSocket.onHandshake(this);
            return;

        } else if (!clientSocket.getIsRegistered()) {
            clientSocket.onCloseConnection(this);
            return;

        } else if (line.equals(CLIENTS_SIGNAL)) {
            game.onGetPlayers(clientSocket);
            return;

        } else if (line.equals(READY_SIGNAL)) {
            game.onPlayerIsReady(clientSocket);
            return;

        } else if (line.equals(PUSH_SIGNAL)) {
            game.onPush(clientSocket);
            return;
        }

        clientSocket.onEchoMessage(this);
    }

    public String getLine() {
        return line;
    }
}
