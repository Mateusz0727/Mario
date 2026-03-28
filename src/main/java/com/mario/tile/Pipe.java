package com.mario.tile;

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
        gc.setFill(Color.rgb(128, 128, 128));
        gc.fillRect(x, y, width, height);
    }

    @Override
    public void tick() {

    }
}
