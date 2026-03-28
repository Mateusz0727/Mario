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
        if (icon != null) {
            gc.drawImage(icon, x, y, width, height);
        } else {
            gc.drawImage(Game.goomba.getImage(), x, y, width, height);
        }
    }

    public void tick() {
        x += velX;
        y += velY;

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
