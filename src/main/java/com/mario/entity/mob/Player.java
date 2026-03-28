package com.mario.entity.mob;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.Entity;
import com.mario.states.PlayerState;
import com.mario.tile.Tile;

import javafx.scene.canvas.GraphicsContext;

public class Player extends Entity {

    public PlayerState state;

    private int pixelsTravelled = 0;

    public Player(double x, double y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, width, height, solid, id, handler);
        state = PlayerState.SMALL;
    }

    public void render(GraphicsContext gc) {
        gc.drawImage(Game.player.getImage(), x, y, width, height);
    }

    public void tick() {
        // Domyślnie zakładamy spadek, jeśli nie skaczemy (kolizja poniżej to skoryguje)
        if (!jumping) falling = true;

        // RUCH I KOLIZJE POZIOME (X)
        x += velX;

        // Zabezpieczenie krawędzi mapy
        if (x <= 0) x = 0;
        if (x + width >= Game.levelWidthPixels) x = Game.levelWidthPixels - width;

        for (Tile t : handler.tile) {
            if (!t.solid && !goingDownPipe) continue;

            if (t.getId() == Id.wall || t.getId() == Id.powerUp || t.getId() == Id.pipe) {
                if (getBoundsLeft().intersects(t.getBounds())) {
                    setVelX(0);
                    x = t.getX() + t.width;
                }
                if (getBoundsRight().intersects(t.getBounds())) {
                    setVelX(0);
                    x = t.getX() - width;
                }
            }
        }

        // RUCH I KOLIZJE PIONOWE (Y)
        y += velY;

        // Zabezpieczenie krawędzi mapy
        if (y <= 0) y = 0;
        if (y + height >= Game.levelHeightPixels) y = Game.levelHeightPixels - height;

        if (goingDownPipe) {
            pixelsTravelled += velY;
        }

        boolean onGround = false;

        for (Tile t : handler.tile) {
            if (!t.solid && !goingDownPipe) continue;

            if (t.getId() == Id.wall || t.getId() == Id.powerUp || t.getId() == Id.pipe) {
                if (getBoundsTop().intersects(t.getBounds())) {
                    setVelY(0);
                    if (jumping) {
                        jumping = false;
                        gravity = 0.0;
                        falling = true;
                    }
                    // Logika uderzenia głową w blok z power-upem
                    if (t.getId() == Id.powerUp) {
                        if (getBoundsTop().intersects(t.getBounds())) {
                            t.activated = true;
                            if (Game.gameClient != null && Game.gameClient.connected) {
                                Game.gameClient.sendPacket(com.mario.net.Packet.tileSync(Game.lobbyCode, (int) t.getX(), (int) t.getY(), false, true));
                            }
                        }
                    }
                    y = t.getY() + t.height;
                }

                if (getBoundsBottom().intersects(t.getBounds())) {
                    setVelY(0);
                    if (falling) falling = false;
                    onGround = true;
                    y = t.getY() - height;
                }
            }

            // Logika monet
            if (getBounds().intersects(t.getBounds()) && t.getId() == Id.coin) {
                Game.coins++;
                t.removed = true;
                if (Game.gameClient != null && Game.gameClient.connected) {
                    Game.gameClient.sendPacket(com.mario.net.Packet.tileSync(Game.lobbyCode, (int) t.getX(), (int) t.getY(), true, false));
                }
                Game.checkForLevelAdvance();
            }
        }

        if (!onGround && !jumping) {
            falling = true;
        }

        // --- INTERAKCJE Z ENTITIES (Grzyby, Goomby) ---
        for (int i = 0; i < handler.entity.size(); i++) {
            Entity e = handler.entity.get(i);

            if (e.getId() == Id.mushroom) {
                if (getBounds().intersects(e.getBounds())) {
                    if (state == PlayerState.SMALL) {
                        double tpX = getX();
                        double tpY = getY();
                        width *= 2;
                        height *= 2;
                        setX(tpX - width);
                        setY(tpY - height);
                        state = PlayerState.BIG;
                    }
                    if (Game.gameClient != null && Game.gameClient.connected) {
                        Game.gameClient.sendPacket(com.mario.net.Packet.entitySync(Game.lobbyCode, e.initX, e.initY, true));
                    }
                    e.die();
                }
            } else if (e.getId() == Id.goomba) {
                if (getBoundsBottom().intersects(e.getBoundsTop())) {
                    Game.goombasDefeated++;
                    if (Game.gameClient != null && Game.gameClient.connected) {
                        Game.gameClient.sendPacket(com.mario.net.Packet.entitySync(Game.lobbyCode, e.initX, e.initY, true));
                    }
                    e.die();
                    Game.checkForLevelAdvance();
                } else if (getBounds().intersects(e.getBounds())) {
                    if (state == PlayerState.BIG) {
                        state = PlayerState.SMALL;
                        width /= 2;
                        height /= 2;
                        x += width;
                        y += height;
                    } else if (state == PlayerState.SMALL) {
                        if (Game.gameClient != null) Game.gameClient.disconnect();
                        die();
                        Game.state = Game.GameState.MENU;
                    }
                }
            }
        }

        // --- FIZYKA SKOKU I GRAWITACJA ---
        if (jumping && !goingDownPipe) {
            gravity -= 1.0;
            setVelY(-gravity);
            if (gravity <= 0.0) {
                jumping = false;
                falling = true;
            }
        }
        if (falling && !goingDownPipe) {
            gravity += 0.7;
            if (gravity > 15.0) gravity = 15.0; // Terminal velocity limit
            setVelY(gravity);
        }

        // --- LOGIKA RUR ---
        if (goingDownPipe) {
            for (int i = 0; i < Game.handler.tile.size(); i++) {
                Tile t = Game.handler.tile.get(i);
                if (t.getId() == Id.pipe) {
                    if (getBounds().intersects(t.getBounds())) {
                        switch (t.facing) {
                            case 0: // Góra
                                setVelY(-5);
                                setVelX(0);
                                break;
                            case 2: // Dół
                                setVelY(5);
                                setVelX(0);
                                break;
                        }
                        if (pixelsTravelled > t.height + height) goingDownPipe = false;
                    }
                }
            }
        }
    }
}
