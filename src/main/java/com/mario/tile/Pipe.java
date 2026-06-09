package com.mario.tile;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Pipe extends Tile {

    public Pipe(double x, double y, int width, int height, boolean solid, Id id, Handler handler, int facing) {
        super(x, y, width, height, solid, id, handler);
        this.facing = facing;
    }

    @Override
    public void render(GraphicsContext gc) {
        if (facing == 1) { // Flip horizontally (facing left)
            gc.save();
            gc.translate(x + width / 2.0, y + height / 2.0);
            gc.scale(-1, 1);
            gc.translate(-(x + width / 2.0), -(y + height / 2.0));
        }

        gc.drawImage(Game.bigPipeTop[1].getImage(), x, y, 64, 64);
        gc.drawImage(Game.bigPipeTop[1].getImage(), x + 64, y, 64, 64);
        gc.drawImage(Game.bigPipeTop[2].getImage(), x + 128, y, 64, 64);
        for(int h=64; h<height; h+=64) {
            gc.drawImage(Game.bigPipeBody[1].getImage(), x, y + h, 64, 64);
            gc.drawImage(Game.bigPipeBody[1].getImage(), x + 64, y + h, 64, 64);
            gc.drawImage(Game.bigPipeBody[2].getImage(), x + 128, y + h, 64, 64);
        }

        if (facing == 1) {
            gc.restore();
        }
    }

    @Override
    public void tick() {

    }
}
