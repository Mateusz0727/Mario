package com.mario.tile;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

public class Trampoline extends Tile {

    private Image texture;

    public Trampoline(int x, int y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, width, height, solid, id, handler);
        this.texture = Game.sheet.getSprite(5, 3);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(texture, getX(), getY(), width, height);
    }

    @Override
    public void tick() {
        // No tick logic needed
    }
}
