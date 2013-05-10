package com.korotovsky.server.core;

import com.korotovsky.server.Bootstrap;
import com.korotovsky.server.client.*;
import com.korotovsky.server.network.ClientSocket;
import com.korotovsky.server.network.protocol.*;
import com.korotovsky.server.network.protocol.responses.ErrorResponse;
import com.korotovsky.server.network.protocol.responses.MessageResponse;
import com.korotovsky.server.network.protocol.responses.PlayersResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Game {
    private Bootstrap bootstrap;
    private Integer counter = 0;
    private Boolean isStarted = false;
    private Boolean isGameEnd = false;

    public Game(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
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

    public Info searchWinner(HashMap<Long, Info> players) {
        for (Map.Entry<Long, Info> entry : players.entrySet()) {
            Info player = entry.getValue();

            if (player.getPlayerGame().getWinner()) {
                return player;
            }
        }

        return new Info(new PlayerGame());
    }

    public Boolean startIsAllReady(HashMap<Long, Info> players) {
        for (Map.Entry<Long, Info> entry : players.entrySet()) {
            Info player = entry.getValue();

            if (!player.getPlayerGame().getReady()) {
                return false;
            }
        }

        start();

        return true;
    }

    /**
     * Callback getting players
     * @param clientSocket ClientSocket
     * @param request Request
     * @throws java.io.IOException
     */
    public void getPlayers(ClientSocket clientSocket, Request request) throws IOException {
        new PlayersResponse(bootstrap.getClients(), clientSocket.getWriter()).send();
    }

    /**
     * Callback player is ready to game
     * @param clientSocket ClientSocket
     * @param request Request
     * @throws IOException
     */
    public synchronized void setPlayerReady(ClientSocket clientSocket, Request request) throws IOException {
        Info player = bootstrap.getClients().get(clientSocket.getId());

        player.getPlayerGame().setReady(true);

        if (startIsAllReady(bootstrap.getClients())) {
            new MessageResponse(clientSocket.getWriter()).setMessage("TODO: Broadcast game start").send();
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
        Info player = bootstrap.getClients().get(clientSocket.getId());

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
                Info winner = searchWinner(bootstrap.getClients());

                new MessageResponse(clientSocket.getWriter()).setMessage("TODO: Broadcast game end").send();
                new MessageResponse(clientSocket.getWriter()).setMessage("Winner: " + winner.getName()).send();
            }
        }

        new MessageResponse(clientSocket.getWriter()).setMessage("Pushed").send();
    }
}
