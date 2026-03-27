package com.mario.entity.powerup;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.Entity;
import com.mario.tile.Tile;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;
import javax.imageio.ImageIO;

public class Mushroom extends Entity {
    private final BufferedImage icon;
    private Random random = new Random();

    public Mushroom(int x, int y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, width, height, solid, id, handler);
        BufferedImage loadedIcon = null;
        try {
            loadedIcon = ImageIO.read(Game.class.getResource("/mushroom.png"));
        } catch (IOException e) {
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

    public void render(Graphics g){
        if (icon != null) {
            g.drawImage(icon, x, y, width, height, null);
        } else {
            g.drawImage(Game.mushroom.getBufferImage(), x, y, width, height, null);
        }
    };

    public void tick() {
        x += velX;
        y += velY;

        boolean onGround = false;

        for(Tile t : handler.tile) {
            if(!t.solid) continue;

            if(getBoundsBottom().intersects(t.getBounds())) {
                onGround = true;
                setVelY(0);
                y = t.getY() - height;
            }

            if(getBoundsLeft().intersects(t.getBounds())) {
                setVelX(-velX);
            }

            if(getBoundsRight().intersects(t.getBounds())) {
                setVelX(-velX);
            }
        }

        if(!onGround) {
            gravity += 0.5;
            setVelY((int) gravity);
            falling = true;
        } else {
            gravity = 0;
            falling = false;
        }
    }
}
