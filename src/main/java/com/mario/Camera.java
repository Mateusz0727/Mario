package com.mario;

import com.mario.entity.Entity;

public class Camera {

    private double x, y;

    public void tick(Entity player) {
        tick(player.getX(), player.getY(), player.width, player.height);
    }

    public void tick(double px, double py, double pwidth, double pheight) {
        double viewportWidth  = Game.WIDTH * Game.SCALE;
        double viewportHeight = Game.HEIGHT * Game.SCALE;

        double targetX = px + pwidth / 2.0 - viewportWidth / 2.0;
        double targetY = py + pheight / 2.0 - viewportHeight / 2.0;

        // Wygładzanie (lerp) - kamera płynie za celem
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
