package com.mario.tile;

import com.mario.Handler;
import com.mario.Id;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public abstract class Tile {
    public double x, y;
    public int width, height;

    public boolean solid;
    public double velX, velY;

    public boolean activated = false;
    public boolean removed = false;
    public int facing = 0;

    public Id id;
    public Handler handler;

    public Tile(double x, double y, int width, int height, boolean solid, Id id, Handler handler) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.solid = solid;
        this.id = id;
        this.handler = handler;
    }

    public abstract void render(GraphicsContext gc);

    public abstract void tick();

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }

    public boolean isSolid() {
        return solid;
    }

    public void setVelX(double velX) {
        this.velX = velX;
    }

    public void setVelY(double velY) {
        this.velY = velY;
    }

    public Id getId() {
        return id;
    }

    public void die() {
        handler.removeTile(this);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(getX(), getY(), width, height);
    }
}
