package com.mario;

import com.mario.entity.Entity;

public class Camera {

    private int x, y;

    public void tick(Entity player) {

        int viewportWidth  = Game.WIDTH * Game.SCALE;
        int viewportHeight = Game.HEIGHT * Game.SCALE;

        x = player.getX() + player.width / 2 - viewportWidth / 2;
        y = player.getY() + player.height / 2 - viewportHeight / 2;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
