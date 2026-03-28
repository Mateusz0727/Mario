package com.mario.tile;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;

import javafx.scene.canvas.GraphicsContext;

public class Wall extends Tile {

    public Wall(double x, double y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, width, height, solid, id, handler);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(Game.grass.getImage(), x, y, width, height);
    }

    @Override
    public void tick() {

    }
}
