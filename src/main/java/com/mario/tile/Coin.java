package com.mario.tile;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;

import javafx.scene.canvas.GraphicsContext;

public class Coin extends Tile {

    public Coin(double x, double y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, width, height, solid, id, handler);
    }

    private int animTime = 0;
    private int animFrame = 0;

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(Game.coinAnim[animFrame].getImage(), x, y, width, height);
    }

    @Override
    public void tick() {
        animTime++;
        if (animTime > 8) { // Szybkość animacji
            animTime = 0;
            animFrame++;
            if (animFrame >= Game.coinAnim.length) {
                animFrame = 0;
            }
        }
    }
}
