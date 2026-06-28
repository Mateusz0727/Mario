package com.mario.entity.mob;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.Entity;
import com.mario.states.PlayerState;
import com.mario.tile.Tile;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class Player extends Entity {

    public PlayerState state;
    public boolean leftPressed = false;
    public boolean rightPressed = false;
    public boolean upPressed = false;
    public boolean onTrampoline = false;

    private int pixelsTravelled = 0;

    // Sprite sheet animation fields
    private static Image smallMarioIdle;
    private static Image[] smallMarioWalk;
    private static Image smallMarioJump;

    private static Image bigMarioIdle;
    private static Image[] bigMarioWalk;
    private static Image bigMarioJump;

    private int animTime = 0;
    private int animFrame = 0;
    public boolean facingRight = true;

    private static void initSprites() {
        if (smallMarioIdle != null) return;
        try {
            Image sheet = new Image(Player.class.getResourceAsStream("/mario_sheet.png"));
            PixelReader reader = sheet.getPixelReader();

            // Small Mario Right (Tight coordinates)
            smallMarioIdle = new WritableImage(reader, 86, 47, 25, 24);
            smallMarioWalk = new Image[] {
                new WritableImage(reader, 137, 48, 25, 23),
                new WritableImage(reader, 189, 47, 25, 24),
                new WritableImage(reader, 241, 47, 24, 24)
            };
            smallMarioJump = new WritableImage(reader, 343, 48, 25, 23);

            // Big Mario Right (Tight coordinates)
            bigMarioIdle = new WritableImage(reader, 86, 189, 24, 35);
            bigMarioWalk = new Image[] {
                new WritableImage(reader, 137, 189, 25, 35),
                new WritableImage(reader, 189, 191, 25, 33),
                new WritableImage(reader, 240, 189, 25, 35)
            };
            bigMarioJump = new WritableImage(reader, 343, 189, 25, 35);

        } catch (Exception e) {
            System.err.println("Could not load player animated sprites: " + e.getMessage());
        }
    }

    public static Image getGhostFrame(int state, boolean jumping, boolean falling, boolean moving, int frame) {
        initSprites();
        if (state == 0) {
            if (jumping || falling) return smallMarioJump;
            else if (moving) return smallMarioWalk[frame % 3];
            else return smallMarioIdle;
        } else {
            if (jumping || falling) return bigMarioJump;
            else if (moving) return bigMarioWalk[frame % 3];
            else return bigMarioIdle;
        }
    }

    public Player(double x, double y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, 48, 48, solid, id, handler);
        state = PlayerState.SMALL;
    }

    public void render(GraphicsContext gc) {
        initSprites();
        Image currentFrame = null;

        if (state == PlayerState.SMALL) {
            if (jumping || falling) {
                currentFrame = smallMarioJump;
            } else if (velX != 0) {
                currentFrame = smallMarioWalk[animFrame];
            } else {
                currentFrame = smallMarioIdle;
            }
        } else {
            if (jumping || falling) {
                currentFrame = bigMarioJump;
            } else if (velX != 0) {
                currentFrame = bigMarioWalk[animFrame];
            } else {
                currentFrame = bigMarioIdle;
            }
        }

        if (currentFrame != null) {
            if (facingRight) {
                gc.drawImage(currentFrame, x, y, width, height);
            } else {
                gc.drawImage(currentFrame, x + width, y, -width, height);
            }
        } else {
            gc.drawImage(Game.player.getImage(), x, y, width, height);
        }
    }


    public void tick() {
        if (leftPressed && !rightPressed) {
            velX = -4; // Delikatnie szybciej (było -3)
        } else if (rightPressed && !leftPressed) {
            velX = 4; // Delikatnie szybciej (było 3)
        } else {
            velX = 0;
        }

        if (upPressed && !jumping && !falling) {
            jumping = true;
            gravity = onTrampoline ? 22.0 : 16.0; // Wyższy skok na trampolinie, inaczej normalny
        }

        // Domyślnie zakładamy spadek, jeśli nie skaczemy (kolizja poniżej to skoryguje)
        if (!jumping) falling = true;

        // RUCH I KOLIZJE POZIOME (X)
        x += velX;

        // Zabezpieczenie krawędzi mapy
        if (x <= 0) x = 0;
        if (x + width >= Game.levelWidthPixels) x = Game.levelWidthPixels - width;

        for (Tile t : handler.tile) {
            if (!t.solid && !goingDownPipe) continue;

            if (t.getId() == Id.wall || t.getId() == Id.powerUp || t.getId() == Id.pipe || t.getId() == Id.trampoline || t.getId() == Id.cloudPipe) {
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
        onTrampoline = false;
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

            if (t.getId() == Id.wall || t.getId() == Id.powerUp || t.getId() == Id.pipe || t.getId() == Id.trampoline || t.getId() == Id.cloudPipe) {
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
                    
                    if (t.getId() == Id.trampoline) {
                        onTrampoline = true;
                    }
                    
                    if (falling) {
                        falling = false;
                        gravity = 0.0;
                    }
                    onGround = true;
                    y = t.getY() - height;
                }
            }

            // Logika monet
            if (getBounds().intersects(t.getBounds()) && t.getId() == Id.coin && !t.removed) {
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
                        double tpY = getY();
                        width = 48;
                        height = 96;
                        setY(tpY - 48);
                        state = PlayerState.BIG;
                    }
                    if (Game.gameClient != null && Game.gameClient.connected) {
                        Game.gameClient.sendPacket(com.mario.net.Packet.entitySync(Game.lobbyCode, e.initX, e.initY, true));
                    }
                    e.die();
                }
            } else if (e.getId() == Id.goomba) {
                Goomba goombaEntity = (Goomba) e;
                if (goombaEntity.dying) {
                    continue;
                }
                if (falling && getBounds().intersects(goombaEntity.getBounds()) && getY() + height / 2 < goombaEntity.getY() + goombaEntity.height / 2) {
                    Game.goombasDefeated++;
                    if (Game.gameClient != null && Game.gameClient.connected) {
                        if (goombaEntity.netId != -1) {
                            Game.gameClient.sendPacket(com.mario.net.Packet.serverGoombaDie(Game.lobbyCode, goombaEntity.netId));
                        } else {
                            Game.gameClient.sendPacket(com.mario.net.Packet.entitySync(Game.lobbyCode, goombaEntity.initX, goombaEntity.initY, true));
                        }
                    }
                    
                    jumping = true;
                    falling = false;
                    gravity = 8.0;

                    goombaEntity.die();
                } else if (getBounds().intersects(e.getBounds())) {
                    if (state == PlayerState.BIG) {
                        state = PlayerState.SMALL;
                        width = 48;
                        height = 48;
                        y += 48;
                        
                        Game.goombasDefeated++;
                        if (Game.gameClient != null && Game.gameClient.connected) {
                            if (goombaEntity.netId != -1) {
                                Game.gameClient.sendPacket(com.mario.net.Packet.serverGoombaDie(Game.lobbyCode, goombaEntity.netId));
                            } else {
                                Game.gameClient.sendPacket(com.mario.net.Packet.entitySync(Game.lobbyCode, goombaEntity.initX, goombaEntity.initY, true));
                            }
                        }
                        goombaEntity.die();
                    } else if (state == PlayerState.SMALL) {
                        die(); // Gracz zostaje usunięty z ekranu w obu trybach
                        if (Game.gameClient != null && Game.gameClient.connected) {
                            // Jesteśmy w trybie Online, zostajemy jako obserwator!
                            com.mario.net.GameData data = new com.mario.net.GameData(
                                    Game.playerName, getX(), getY(), false, false, 2, facingRight ? 1 : 0);
                            Game.gameClient.sendPacket(com.mario.net.Packet.update(Game.lobbyCode, data));
                        } else {
                            // Jesteśmy w trybie Single Player, koniec gry
                            Game.state = Game.GameState.GAME_OVER;
                        }
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

        // --- ANIMATION UPDATE ---
        if (velX > 0) {
            facingRight = true;
        } else if (velX < 0) {
            facingRight = false;
        }

        if (velX != 0 && !jumping && !falling) {
            animTime++;
            if (animTime > 5) {
                animTime = 0;
                animFrame++;
                if (animFrame >= 3) {
                    animFrame = 0;
                }
            }
        } else {
            animFrame = 0;
            animTime = 0;
        }
    }
}

