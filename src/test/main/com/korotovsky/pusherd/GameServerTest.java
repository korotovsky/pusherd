package com.korotovsky.pusherd;

import org.junit.Assert;
import org.junit.Test;

import java.util.logging.Logger;

public class GameServerTest {

    @Test
    public void testGetLogger() {
        Logger logger = Logger.getLogger("pusherd");
        GameServer gameServer = new GameServer(logger);

        Assert.assertEquals(logger, gameServer.getLogger());
    }
}
