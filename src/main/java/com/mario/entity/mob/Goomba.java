package com.mario.entity.mob;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.Entity;
import com.mario.tile.Tile;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.util.Random;

public class Goomba extends Entity {

    private final Image icon;
    private Random random = new Random();
    public boolean dying = false;
    public double serverX, serverY;

    public Goomba(double x, double y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, width, height, solid, id, handler);

        Image loadedIcon = null;
        try {
            loadedIcon = new Image(Game.class.getResourceAsStream("/goomba.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.icon = loadedIcon;

        // Zawsze idź w stronę środka poziomu
        if (x < Game.levelWidthPixels / 2) {
            setVelX(2); // Start po lewej -> idź w prawo
        } else {
            setVelX(-2); // Start po prawej -> idź w lewo
        }
    }
    @Override
    public javafx.geometry.Rectangle2D getBoundsTop() {
        return new javafx.geometry.Rectangle2D(getX(), getY() - 0.2, width, 5.2);
    }

    public void render(GraphicsContext gc) {
        if (dying) {
            if (icon != null) {
                gc.drawImage(icon, x, y + height, width, -height);
            } else {
                gc.drawImage(Game.goomba.getImage(), x, y + height, width, -height);
            }
        } else {
            if (icon != null) {
                gc.drawImage(icon, x, y, width, height);
            } else {
                gc.drawImage(Game.goomba.getImage(), x, y, width, height);
            }
        }
        drawHitboxes(gc);
    }

    @Override
    public void die() {
        if (!dying) {
            dying = true;
            solid = false;
            setVelX(0);
            setVelY(-5); // Pop up slightly when stomped
        }
    }

    public void tick() {
        if (Game.menuIndex == 1 && netId != -1) {
            // Zwiększony współczynnik LERP z 0.15 na 0.5, aby zapobiec 
            // nienaturalnemu zwalnianiu tuż przed ziemią.
            x += (serverX - x) * 0.5;
            y += (serverY - y) * 0.5;
            
            // Jeśli jest bardzo blisko celu, przyciągnij go na sztywno, 
            // by uniknąć asympotycznego zwalniania przy podłożu.
            if (Math.abs(serverX - x) < 1.0) x = serverX;
            if (Math.abs(serverY - y) < 1.0) y = serverY;
            
            if (dying && y > Game.levelHeightPixels + 100) {
                handler.removeEntity(this);
            }
            return; // Skip local physics!
        }

        if (dying) {
            setVelY(velY + 0.5); // Apply simple gravity
            x += velX;
            y += velY;
            if (y > Game.levelHeightPixels + 100) {
                handler.removeEntity(this); // Remove once off screen
            }
            return;
        }

        x += velX;
        y += velY;
        
        if (x <= 0) {
            x = 0;
            velX = -velX;
        }
        if (x + width >= Game.levelWidthPixels) {
            x = Game.levelWidthPixels - width;
            velX = -velX;
        }

        boolean onGround = false;

        for (Tile t : handler.tile) {
            if (!t.solid || t.getId() == Id.coin) continue;

            if (getBoundsBottom().intersects(t.getBounds())) {
                onGround = true;
                setVelY(0);
                y = t.getY() - height;
            }

            if (getBoundsLeft().intersects(t.getBounds())) {
                setVelX(-velX);
            }

            if (getBoundsRight().intersects(t.getBounds())) {
                setVelX(-velX);
            }
        }

        if (!onGround) {
            gravity += 0.5;
            setVelY((int) gravity);
            falling = true;
        } else {
            gravity = 0;
            falling = false;
        }
    }
}
