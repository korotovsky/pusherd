package com.korotovsky.pusherd.core;

public class PlayerGame {
    private Integer counter = 0;
    private Boolean isReady = false;
    private Boolean isWinner = false;

    public void increment() {
        if (isReady && !isWinner) {
            counter++;

            if (counter.equals(Game.WIN_LIMIT)) {
                setWinner(true);
            }
        }
    }

    public Boolean getReady() {
        return isReady;
    }

    public void setReady(Boolean ready) {
        isReady = ready;
    }

    public Boolean getWinner() {
        return isWinner;
    }

    public void setWinner(Boolean winner) {
        isWinner = winner;
    }
}
