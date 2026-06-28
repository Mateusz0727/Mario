package com.mario.tile;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.GraphicsContext;

public class CloudPipe extends Tile {

    public int facing;

    public CloudPipe(double x, double y, int width, int height, boolean solid, Id id, Handler handler, int facing) {
        super(x, y, width, height, solid, id, handler);
        this.facing = facing;
    }

    @Override
    public void render(GraphicsContext gc) {
        int drawY = (int) y;
        if (facing == 2) {
            drawY -= 96; // Przesunięcie o 1 płytkę (64) + 32 pixele w górę
            
            gc.save();
            gc.translate(x + width / 2.0, drawY + height / 2.0);
            gc.rotate(180);
            gc.translate(-(x + width / 2.0), -(drawY + height / 2.0));
        }

        gc.drawImage(Game.pipeTop[0].getImage(), x, drawY, 64, 64);
        gc.drawImage(Game.pipeTop[1].getImage(), x + 64, drawY, 64, 64);
        for(int h=64; h<height; h+=64) {
            gc.drawImage(Game.pipeBody[0].getImage(), x, drawY + h, 64, 64);
            gc.drawImage(Game.pipeBody[1].getImage(), x + 64, drawY + h, 64, 64);
        }

        if (facing == 2) {
            gc.restore();
        }
    }

    @Override
    public void tick() {
    }

    @Override
    public Rectangle2D getBounds() {
        if (facing == 2) {
            return new Rectangle2D(x, y - 96, width, height);
        }
        return super.getBounds();
    }
}
