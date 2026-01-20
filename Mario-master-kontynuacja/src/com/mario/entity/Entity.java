package com.mario.entity;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.tile.Tile;

import java.awt.*;

public abstract class Entity {
    public int x,y;
    public int width,height;

    public boolean solid;
    public boolean jumping=false;
    public boolean falling = true;
    public boolean removed = false;
    public double velX,velY;
    public double gravity =0.0;
    public Id id;
    public boolean goingDownPipe = false;

    public Handler handler;



    public Entity(int x, int y, int width, int height, boolean solid, Id id, Handler handler) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.solid = solid;
        this.id = id;
        this.handler = handler;
    }

    public abstract void render(Graphics g);

    public  abstract void tick();

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
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
        if(getId()==Id.player){
            Game.lives--;
            Game.showDeathScreen = true;
            if(Game.lives==0) Game.gameOver = true;
        }
    }
    public Rectangle getBounds() {
        return  new Rectangle(getX(),getY(),width,height);
    }
    public Rectangle getBoundsTop() {
        return  new Rectangle(getX()+10,getY(),width-20,5);
    }
    public Rectangle getBoundsBottom() {
        return new Rectangle(
                getX() + 10,
                getY() + height - 5,
                width - 20,
                5
        );
    }
    public Rectangle getBoundsLeft() {
        return  new Rectangle(getX(),getY()+10,5,height-20);
    }
    public Rectangle getBoundsRight() {
        return  new Rectangle(getX()+width-5,getY()+10,5,height-20);
    }
}
