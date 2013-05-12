package com.korotovsky.pusherd.core;

import com.korotovsky.pusherd.GameServer;
import com.korotovsky.pusherd.client.*;
import com.korotovsky.pusherd.events.GameEvents;
import com.korotovsky.pusherd.network.ClientSocket;
import com.korotovsky.pusherd.network.protocol.responses.game.*;
import com.korotovsky.pusherd.network.protocol.responses.system.ErrorResponse;
import com.korotovsky.pusherd.network.protocol.responses.system.MessageResponse;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Game implements GameEvents {
    public static Integer WIN_LIMIT = 10;
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
        return isStarted && counter.equals(WIN_LIMIT);
    }

    private Player searchWinner(ConcurrentHashMap<ClientSocket, Player> players) {
        for (Map.Entry<ClientSocket, Player> entry : players.entrySet()) {
            Player player = entry.getValue();

            if (player.getPlayerGame().getWinner()) {
                return player;
            }
        }

        return new Player(new PlayerGame());
    }

    private Boolean startIsAllReady() {
        ConcurrentHashMap<ClientSocket, Player> players = gameServer.getPlayers();

        for (Map.Entry<ClientSocket, Player> entry : players.entrySet()) {
            Player player = entry.getValue();

            if (!player.getPlayerGame().getReady()) {
                return false;
            }
        }

        start();

        return true;
    }

    /**
     * Callback on new player connected to the game (after handshake)
     *
     * @param clientSocket ClientSocket
     * @throws java.io.IOException
     */
    public void onPlayerConnected(ClientSocket clientSocket, Player player) throws IOException {
        ConcurrentHashMap<ClientSocket, Player> players = gameServer.getPlayers();

        for (Map.Entry<ClientSocket, Player> entry : players.entrySet()) {
            ClientSocket cs = entry.getKey();
            if (clientSocket.equals(cs)) {
                continue;
            }

            new PlayerConnectedResponse(player, cs.getWriter()).send();
        }
    }

    /**
     * Callback on player disconnected from the game
     *
     * @param clientSocket ClientSocket
     * @throws java.io.IOException
     */
    public void onPlayerDisconnected(ClientSocket clientSocket, Player player) throws IOException {
        ConcurrentHashMap<ClientSocket, Player> players = gameServer.getPlayers();

        for (Map.Entry<ClientSocket, Player> entry : players.entrySet()) {
            ClientSocket cs = entry.getKey();
            if (clientSocket.equals(cs)) {
                continue;
            }

            new PlayerDisconnectedResponse(player, cs.getWriter()).send();
        }
    }

    /**
     * Callback getting players
     *
     * @param clientSocket ClientSocket
     * @throws java.io.IOException
     */
    public void onGetPlayers(ClientSocket clientSocket) throws IOException {
        new PlayersResponse(gameServer.getPlayers(), clientSocket.getWriter()).send();
    }

    /**
     * Callback player is ready to game
     *
     * @param clientSocket ClientSocket
     * @throws IOException
     */
    public synchronized void onPlayerIsReady(ClientSocket clientSocket) throws IOException {
        Player player = gameServer.getPlayers().get(clientSocket);
        ConcurrentHashMap<ClientSocket, Player> players = gameServer.getPlayers();

        player.getPlayerGame().setReady(true);

        new MessageResponse(clientSocket.getWriter()).setMessage("Ready").send();

        if (gameServer.getPlayersCount() > 1 && startIsAllReady()) {
            for (Map.Entry<ClientSocket, Player> entry : players.entrySet()) {
                ClientSocket cs = entry.getKey();

                new GameStartResponse(cs.getWriter()).send();
            }
        }
    }

    /**
     * Callback increase push-count for player
     *
     * @param clientSocket ClientSocket
     * @throws IOException
     */
    public void onPush(ClientSocket clientSocket) throws IOException {
        Player player = gameServer.getPlayers().get(clientSocket);

        if (!player.getPlayerGame().getReady()) {
            new ErrorResponse(clientSocket.getWriter()).setMessage("You are not ready").send();
            return;

        } else if (!isGameStarted()) {
            new ErrorResponse(clientSocket.getWriter()).setMessage("Game not started yet").send();
            return;

        } else if (isGameEnded()) {
            new ErrorResponse(clientSocket.getWriter()).setMessage("Game already ended").send();
            return;

        } else if (gameServer.getPlayersCount() < 2) {
            new ErrorResponse(clientSocket.getWriter()).setMessage("Not enough players to start game").send();
            return;
        }

        synchronized (this) {
            player.getPlayerGame().increment();
            increment();

            if (isGameEnded()) {
                Player winner = searchWinner(gameServer.getPlayers());

                ConcurrentHashMap<ClientSocket, Player> players = gameServer.getPlayers();
                for (Map.Entry<ClientSocket, Player> entry : players.entrySet()) {
                    ClientSocket cs = entry.getKey();

                    new GameEndResponse(cs.getWriter()).send();
                    new GameWinnerResponse(winner, cs.getWriter()).send();
                }
            } else {
                new MessageResponse(clientSocket.getWriter()).setMessage("Pushed").send();
            }
        }
    }
}
