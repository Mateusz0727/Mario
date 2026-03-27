package com.mario.io;

import java.io.Serializable;

public class GameData implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int coins;
    private final int goombasDefeated;
    private final int lives;
    private final String level;

    public GameData(int coins, int goombasDefeated, int lives, String level) {
        this.coins = coins;
        this.goombasDefeated = goombasDefeated;
        this.lives = lives;
        this.level = level;
    }

    public int getCoins() {
        return coins;
    }

    public int getGoombasDefeated() {
        return goombasDefeated;
    }

    public int getLives() {
        return lives;
    }

    public String getLevel() {
        return level;
    }
}
