package com.mario.tile;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.powerup.Mushroom;
import com.mario.gfx.Sprite;

import java.awt.*;

public class PowerUpBlock extends Tile{
    private Sprite powerUp;

    private boolean poppedUp = false;

    private int spriteY = getY();

    public PowerUpBlock(int x, int y, int width, int height, boolean solid, Id id, Handler handler, Sprite powerUp) {
        super(x, y, width, height, solid, id, handler);
        this.powerUp = powerUp;
    }

    @Override
    public void render(Graphics g) {
        if(!poppedUp) g.drawImage(powerUp.getBufferImage(), x, y, width, height, null);
        if(!activated) g.drawImage(Game.powerUp.getBufferImage(),x,y,width,height,null);
        else g.drawImage(Game.usedPowerUp.getBufferImage(),x,y,width,height,null);
    }

    @Override
    public void tick() {
        if(activated && !poppedUp) {
            spriteY--;
            if(spriteY <= y-height) {
                handler.addEntity(new Mushroom(x,spriteY,width, height, true, Id.mushroom, handler));
                poppedUp = true;
            }
        }
    }

}
