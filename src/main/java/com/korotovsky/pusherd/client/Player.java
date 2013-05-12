package com.korotovsky.pusherd.client;

import com.korotovsky.pusherd.core.PlayerGame;
import com.korotovsky.pusherd.events.PlayerGameEvents;

public class Player implements PlayerGameEvents {
    private Integer version;
    private String name;
    private PlayerGame playerGame;

    public Player(PlayerGame playerGame) {
        this.playerGame = playerGame;
    }

    public PlayerGame getPlayerGame() {
        return playerGame;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
