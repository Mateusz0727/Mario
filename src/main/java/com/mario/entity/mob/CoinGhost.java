package com.mario.entity.mob;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.Entity;
import com.mario.tile.Tile;
import com.mario.tile.Coin;
import javafx.scene.canvas.GraphicsContext;

public class CoinGhost extends Entity {

    private int coinDropTimer = 0;
    private int teleportCooldown = 0;
    private boolean teleportDirectionRight = false;

    public CoinGhost(double x, double y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, width, height, solid, id, handler);

        int dir = (((int)x / 64) + ((int)y / 64)) % 2;
        if (dir == 0) setVelX(-2);
        else setVelX(2);
    }

    @Override
    public void render(GraphicsContext gc) {
        gc.drawImage(Game.coinGhostSprite.getImage(), x, y, width, height);
    }

    @Override
    public void tick() {
        if (teleportCooldown > 0) {
            teleportCooldown--;
        }

        x += velX;
        y += velY;

        velY += 0.8;
        if (velY > 15.0) velY = 15.0;

        if (x <= 0) {
            x = 0;
            velX = -velX;
        }
        if (x + width >= Game.levelWidthPixels) {
            x = Game.levelWidthPixels - width;
            velX = -velX;
        }

        for (Tile t : handler.tile) {
            if (t.getId() == Id.cloudPipe && teleportCooldown == 0) {
                if (getBounds().intersects(t.getBounds())) {
                    for (Tile other : handler.tile) {
                        if (other.getId() == Id.cloudPipe && other != t) {
                            if (other.getY() == 0) { // Top pipe
                                x = other.getX() + (other.width / 2.0) - (width / 2.0);
                                y = 192; // Dolna granica wizualna wydłużonej górnej rury
                            } else {
                                x = other.getX();
                                y = other.getY();
                            }
                            velX = teleportDirectionRight ? Math.abs(velX) : -Math.abs(velX);
                            teleportDirectionRight = !teleportDirectionRight;
                            teleportCooldown = 60; // 1 sekunda opóźnienia, żeby nie utknął w pętli
                            break;
                        }
                    }
                }
            }

            if (!t.solid || t.getId() == Id.coin || t.getId() == Id.cloudPipe) continue;

            if (getBoundsBottom().intersects(t.getBounds())) {
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

        // Drop coin every 5 seconds (60 ticks * 5 = 300)
        coinDropTimer++;
        if (coinDropTimer >= 300) {
            coinDropTimer = 0;
            handler.addTile(new Coin(x, y, 64, 64, true, Id.coin, handler));
        }
    }
}
