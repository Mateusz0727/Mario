package com.mario.entity.mob;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.Entity;
import com.mario.tile.Tile;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Random;

public class Goomba extends Entity {

    private final Image icon;
    private Random random = new Random();
    public boolean dying = false;

    public Goomba(double x, double y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, width, height, solid, id, handler);

        Image loadedIcon = null;
        try {
            loadedIcon = new Image(Game.class.getResourceAsStream("/goomba.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.icon = loadedIcon;

        int dir = random.nextInt(2);

        switch (dir) {
            case 0:
                setVelX(-2);
                break;

            case 1:
                setVelX(2);
                break;
        }
    }

    public void render(GraphicsContext gc) {
        if (dying) {
            if (icon != null) {
                gc.drawImage(icon, x, y + height, width, -height);
            } else {
                gc.drawImage(Game.goomba.getImage(), x, y + height, width, -height);
            }
        } else {
            if (icon != null) {
                gc.drawImage(icon, x, y, width, height);
            } else {
                gc.drawImage(Game.goomba.getImage(), x, y, width, height);
            }
        }
    }

    @Override
    public void die() {
        if (!dying) {
            dying = true;
            solid = false;
            setVelX(0);
            setVelY(-5); // Pop up slightly when stomped
        }
    }

    public void tick() {
        if (dying) {
            setVelY(velY + 0.5); // Apply simple gravity
            x += velX;
            y += velY;
            if (y > Game.levelHeightPixels + 100) {
                handler.removeEntity(this); // Remove once off screen
            }
            return;
        }

        x += velX;
        y += velY;
        
        if (x <= 0) {
            x = 0;
            velX = -velX;
        }
        if (x + width >= Game.levelWidthPixels) {
            x = Game.levelWidthPixels - width;
            velX = -velX;
        }

        boolean onGround = false;

        for (Tile t : handler.tile) {
            if (!t.solid) continue;

            if (getBoundsBottom().intersects(t.getBounds())) {
                onGround = true;
                setVelY(0);
                y = t.getY() - height;
            }

            if (getBoundsLeft().intersects(t.getBounds())) {
                setVelX(-velX);
            }

            if (getBoundsRight().intersects(t.getBounds())) {
                setVelX(-velX);
            }
        }

        if (!onGround) {
            gravity += 0.5;
            setVelY((int) gravity);
            falling = true;
        } else {
            gravity = 0;
            falling = false;
        }
    }
}
