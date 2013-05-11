package com.korotovsky.server.core;

import com.korotovsky.server.GameServer;
import com.korotovsky.server.client.*;
import com.korotovsky.server.network.ClientSocket;
import com.korotovsky.server.network.protocol.*;
import com.korotovsky.server.network.protocol.responses.ErrorResponse;
import com.korotovsky.server.network.protocol.responses.MessageResponse;
import com.korotovsky.server.network.protocol.responses.PlayersResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class Game {
    private GameServer gameServer;
    private Integer counter = 0;
    private Boolean isStarted = false;
    private Boolean isGameEnd = false;

    public Game(GameServer gameServer) {
        this.gameServer = gameServer;
    }

    public void increment() {
        if (isStarted) {
            counter++;
        }
    }

    public void start() {
        isStarted = true;
        isGameEnd = false;
    }

    public void stop() {
        isGameEnd = true;
        counter = 0;
    }

    public Boolean isGameStarted() {
        return isStarted;
    }

    public Boolean isGameEnded() {
        return isStarted && counter == 100;
    }

    public Info searchWinner(HashMap<Integer, Info> players) {
        for (Map.Entry<Integer, Info> entry : players.entrySet()) {
            Info player = entry.getValue();

            if (player.getPlayerGame().getWinner()) {
                return player;
            }
        }

        return new Info(new PlayerGame());
    }

    public Boolean startIsAllReady() {
        HashMap<Integer, Info> players = gameServer.getClients();

        for (Map.Entry<Integer, Info> entry : players.entrySet()) {
            Info player = entry.getValue();

            if (!player.getPlayerGame().getReady()) {
                return false;
            }
        }

        start();

        return true;
    }

    public void notifyAllPlayers() {
        HashMap<Integer, Info> players = gameServer.getClients();
        ExecutorService clientSockets = gameServer.getWorkers();

        for (Map.Entry<Integer, Info> entry : players.entrySet()) {
            Info player = entry.getValue();

        }

    }

    /**
     * Callback getting players
     * @param clientSocket ClientSocket
     * @param request Request
     * @throws java.io.IOException
     */
    public void getPlayers(ClientSocket clientSocket, Request request) throws IOException {
        new PlayersResponse(gameServer.getClients(), clientSocket.getWriter()).send();
    }

    /**
     * Callback player is ready to game
     * @param clientSocket ClientSocket
     * @param request Request
     * @throws IOException
     */
    public synchronized void setPlayerReady(ClientSocket clientSocket, Request request) throws IOException {
        Info player = gameServer.getClients().get(clientSocket.hashCode());

        player.getPlayerGame().setReady(true);

        if (startIsAllReady()) {
            notifyAllPlayers();
        }

        new MessageResponse(clientSocket.getWriter()).setMessage("Ready").send();
    }

    /**
     * Callback increase push-count for player
     * @param clientSocket ClientSocket
     * @param request Request
     * @throws IOException
     */
    public void setPlayerPush(ClientSocket clientSocket, Request request) throws IOException {
        Info player = gameServer.getClients().get(clientSocket.hashCode());

        if (!player.getPlayerGame().getReady()) {
            new ErrorResponse(clientSocket.getWriter()).setMessage("You are not ready").send();
            return;

        } else if (!isGameStarted()) {
            new ErrorResponse(clientSocket.getWriter()).setMessage("Game not started yet").send();
            return;

        } else if (isGameEnded()) {
            new ErrorResponse(clientSocket.getWriter()).setMessage("Game already ended").send();
            return;

        }

        synchronized (this) {
            player.getPlayerGame().increment();
            increment();

            if (isGameEnded()) {
                Info winner = searchWinner(gameServer.getClients());

                new MessageResponse(clientSocket.getWriter()).setMessage("TODO: Broadcast game end").send();
                new MessageResponse(clientSocket.getWriter()).setMessage("Winner: " + winner.getName()).send();
            }
        }

        new MessageResponse(clientSocket.getWriter()).setMessage("Pushed").send();
    }
}
