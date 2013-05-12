package com.korotovsky.pusherd;

import com.korotovsky.pusherd.client.Player;
import com.korotovsky.pusherd.core.Game;
import com.korotovsky.pusherd.core.PlayerGame;
import com.korotovsky.pusherd.network.ClientSocket;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

public class GameServerTest {
    private static GameServer gameServer;

    @Before
    public void initServer() {
        gameServer = new GameServer(Logger.getLogger("pusherd"));
    }

    @Test
    public void testGetLogger() {
        Assert.assertThat(gameServer.getLogger(), CoreMatchers.instanceOf(Logger.class));
    }

    @Test
    public void testGetGame() {
        Assert.assertThat(gameServer.getGame(), CoreMatchers.instanceOf(Game.class));
    }

    @Test
    public void testGetPlayers() {
        Assert.assertThat(gameServer.getPlayers(), CoreMatchers.instanceOf(ConcurrentHashMap.class));
    }

    @Test
    public void testGetWorkers() {
        Assert.assertThat(gameServer.getWorkers(), CoreMatchers.instanceOf(ExecutorService.class));
    }

    @Test
    public void testIsAlive() {
        Assert.assertEquals(true, gameServer.isAlive());
    }

    @Test
    public void testGetPlayersCount() {
        Assert.assertEquals(new Integer(0), gameServer.getPlayersCount());
    }

    @Test
    public void testOnPutClient() {
        gameServer.onPutClient(new ClientSocket(gameServer, new Socket()), new Player(new PlayerGame()));

        Assert.assertEquals(new Integer(1), gameServer.getPlayersCount());
    }

    @Test
    public void testOnRemoveClient() {
        ClientSocket clientSocket = new ClientSocket(gameServer, new Socket());

        gameServer.onPutClient(clientSocket, new Player(new PlayerGame()));

        Assert.assertEquals(new Integer(1), gameServer.getPlayersCount());

        gameServer.onRemoveClient(clientSocket);

        Assert.assertEquals(new Integer(0), gameServer.getPlayersCount());
    }
}
