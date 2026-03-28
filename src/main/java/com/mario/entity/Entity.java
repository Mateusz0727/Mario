package com.mario.entity;

import com.mario.Handler;
import com.mario.Id;
import com.mario.tile.Tile;

import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public abstract class Entity {
    public double x, y;
    public int width, height;

    public boolean solid;
    public boolean jumping = false;
    public boolean falling = true;
    public boolean goingDownPipe = false;

    public double velX, velY;
    public double gravity = 0.0;
    public Id id;
    
    public int initX, initY; // Sieciowy identyfikator pozycji startowej

    public Handler handler;

    public Entity(double x, double y, int width, int height, boolean solid, Id id, Handler handler) {
        this.x = x;
        this.y = y;
        this.initX = (int) x;
        this.initY = (int) y;
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
        handler.removeEntity(this);
    }

    public Rectangle2D getBounds() {
        return new Rectangle2D(getX(), getY(), width, height);
    }

    public Rectangle2D getBoundsTop() {
        return new Rectangle2D(getX() + 10, getY() - 0.2, width - 20, 5.2);
    }

    public Rectangle2D getBoundsBottom() {
        return new Rectangle2D(getX() + 10, getY() + height - 5, width - 20, 5.2);
    }

    public Rectangle2D getBoundsLeft() {
        return new Rectangle2D(getX() - 0.2, getY() + 10, 5.2, height - 20);
    }

    public Rectangle2D getBoundsRight() {
        return new Rectangle2D(getX() + width - 5, getY() + 10, 5.2, height - 20);
    }
}
