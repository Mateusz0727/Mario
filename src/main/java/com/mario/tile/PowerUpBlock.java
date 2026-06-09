package com.mario.tile;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.powerup.Mushroom;
import com.mario.gfx.Sprite;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class PowerUpBlock extends Tile {
    private Image powerUp;

    private boolean poppedUp = false;

    private double spriteY = getY();

    public PowerUpBlock(double x, double y, int width, int height, boolean solid, Id id, Handler handler, Image powerUp) {
        super(x, y, width, height, solid, id, handler);
        this.powerUp = powerUp;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (activated && !poppedUp) {
            gc.drawImage(powerUp, x, spriteY, width, height);
        }
        
        if (!activated) {
            gc.drawImage(Game.powerUp.getImage(), x, y, width, height);
        } else {
            gc.drawImage(Game.usedPowerUp.getImage(), x, y, width, height);
        }
    }

    private int refreshTimer = 0;

    @Override
    public void tick() {
        if (activated && !poppedUp) {
            spriteY--;
            if (spriteY <= y - height) {
                Mushroom m = new Mushroom(x, spriteY, width, height, true, Id.mushroom, handler);
                m.initY = (int) y; // Stabilny identyfikator oparty na pozycji bloku
                handler.addEntity(m);
                poppedUp = true;
            }
        }
        
        if (poppedUp) {
            refreshTimer++;
            if (refreshTimer >= 1800) { // 30 sekund w 60 FPS
                activated = false;
                poppedUp = false;
                spriteY = getY();
                refreshTimer = 0;
            }
        }
    }
}
