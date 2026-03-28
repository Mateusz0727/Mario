package com.mario;

import com.mario.entity.Entity;
import com.mario.tile.Tile;
import com.mario.tile.Wall;

import javafx.scene.canvas.GraphicsContext;

import java.util.LinkedList;

public class Handler {
    public LinkedList<Entity> entity = new LinkedList<Entity>();
    public LinkedList<Tile> tile = new LinkedList<Tile>();

    public Handler() {
        // level creation is now handled manually by passing an Image
    }

    public void render(GraphicsContext gc) {
        for (Entity e : entity) {
            e.render(gc);
        }

        for (Tile tile : tile) {
            tile.render(gc);
        }
    }

    public void tick() {
        for (int i = 0; i < entity.size(); i++) {
            entity.get(i).tick();
        }
        for (int i = 0; i < tile.size(); i++) {
            Tile t = tile.get(i);
            t.tick();
            if (t.removed) {
                tile.remove(i);
                i--;
            }
        }
    }

    public void addEntity(Entity entity) {
        this.entity.add(entity);
    }

    public void removeEntity(Entity entity) {
        this.entity.remove(entity);
    }

    public void addTile(Tile tile) {
        this.tile.add(tile);
    }

    public void removeTile(Tile tile) {
        this.tile.remove(tile);
    }

    public void createLevel(javafx.scene.image.Image levelImage) {
        javafx.scene.image.PixelReader pr = levelImage.getPixelReader();
        int width = (int) levelImage.getWidth();
        int height = (int) levelImage.getHeight();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                javafx.scene.paint.Color color = pr.getColor(x, y);
                int r = (int) (color.getRed() * 255);
                int g = (int) (color.getGreen() * 255);
                int b = (int) (color.getBlue() * 255);
                int a = (int) (color.getOpacity() * 255);

                if (a == 0) continue; // Skip fully transparent pixels

                if (r == 0 && g == 0 && b == 0) { // Black -> Wall
                    addTile(new Wall(x * 64, y * 64, 64, 64, true, Id.wall, this));
                } else if (r == 0 && g == 0 && b == 255) { // Blue -> Player
                    addEntity(new com.mario.entity.mob.Player(x * 64, y * 64, 64, 64, true, Id.player, this));
                } else if (r == 255 && g == 0 && b == 0) { // Red -> Goomba
                    addEntity(new com.mario.entity.mob.Goomba(x * 64, y * 64, 64, 64, true, Id.goomba, this));
                } else if (r == 255 && g == 119 && b == 0) { // Orange -> Mushroom
                    addEntity(new com.mario.entity.powerup.Mushroom(x * 64, y * 64, 64, 64, true, Id.mushroom, this));
                } else if (r == 255 && g == 250 && b == 0) { // Pale Yellow -> Coin
                    addTile(new com.mario.tile.Coin(x * 64, y * 64, 64, 64, true, Id.coin, this));
                } else if (r == 255 && g == 255 && b == 0) { // Yellow -> PowerUpBlock (Mushroom)
                    addTile(new com.mario.tile.PowerUpBlock(x * 64, y * 64, 64, 64, true, Id.powerUp, this, Game.mushroom));
                }
            }
        }
    }

    public Entity findPlayer() {
        for (Entity e : entity) {
            if (e.getId() == Id.player) {
                return e;
            }
        }
        return null; // Return null if player not found
    }

    public synchronized void updateTileState(int x, int y, boolean removed, boolean activated) {
        for (Tile t : tile) {
            if (t.getX() == x && t.getY() == y) {
                if (removed) t.removed = true;
                if (activated) t.activated = true;
                break;
            }
        }
    }

    public synchronized void updateEntityState(int initX, int initY, boolean removed) {
        for (int i = 0; i < entity.size(); i++) {
            Entity e = entity.get(i);
            if (e.initX == initX && e.initY == initY) {
                if (removed) {
                    entity.remove(i);
                    i--;
                }
                break;
            }
        }
    }
}
