package com.korotovsky.server.client;

import com.korotovsky.server.core.PlayerGame;

public class Info {
    private Integer version;
    private String name;
    private PlayerGame playerGame;

    public Info(PlayerGame playerGame) {
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
