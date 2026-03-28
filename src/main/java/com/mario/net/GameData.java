package com.mario.net;

import java.io.Serializable;

/**
 * GameData to paczka danych przesyłana z użyciem Javy Serializacji pomiędzy Serwerem a Klientem.
 * Musi pakietowo należeć do tego samego namespace'u (com.mario.net) wszędzie.
 */
public class GameData implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public String playerId;
    public double x, y;
    public boolean jumping, falling;
    public int state; // np. 0 = SMALL, 1 = BIG
    public int facing; // np. 0 = LEFT, 1 = RIGHT
    
    public GameData(String playerId, double x, double y, boolean jumping, boolean falling, int state, int facing) {
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.jumping = jumping;
        this.falling = falling;
        this.state = state;
        this.facing = facing;
    }
}
