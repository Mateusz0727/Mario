package com.mario.tile;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.powerup.Mushroom;
import com.mario.gfx.Sprite;

import javafx.scene.canvas.GraphicsContext;

public class PowerUpBlock extends Tile {
    private Sprite powerUp;

    private boolean poppedUp = false;

    private double spriteY = getY();

    public PowerUpBlock(double x, double y, int width, int height, boolean solid, Id id, Handler handler, Sprite powerUp) {
        super(x, y, width, height, solid, id, handler);
        this.powerUp = powerUp;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (activated && !poppedUp) {
            gc.drawImage(powerUp.getImage(), x, spriteY, width, height);
        }
        
        if (!activated) {
            gc.drawImage(Game.powerUp.getImage(), x, y, width, height);
        } else {
            gc.drawImage(Game.usedPowerUp.getImage(), x, y, width, height);
        }
    }

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
    }
}
