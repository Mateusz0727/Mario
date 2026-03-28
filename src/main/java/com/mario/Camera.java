package com.mario;

import com.mario.entity.Entity;

public class Camera {

    private double x, y;

    public void tick(Entity player) {
        double viewportWidth  = Game.WIDTH * Game.SCALE;
        double viewportHeight = Game.HEIGHT * Game.SCALE;

        double targetX = player.getX() + player.width / 2.0 - viewportWidth / 2.0;
        double targetY = player.getY() + player.height / 2.0 - viewportHeight / 2.0;

        // Wygładzanie (lerp) - kamera płynie za graczem
        x += (targetX - x) * 0.1;
        y += (targetY - y) * 0.1;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }
}
