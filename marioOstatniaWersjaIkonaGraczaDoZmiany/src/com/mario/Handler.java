package com.mario;

import com.mario.entity.Entity;
import com.mario.entity.mob.Goomba;
import com.mario.entity.mob.Player;
import com.mario.entity.powerup.Mushroom;
import com.mario.tile.*;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedList;

public class Handler {
    public LinkedList<Entity> entity = new LinkedList<Entity>();
    public LinkedList<Tile> tile = new LinkedList<Tile>();

        public Handler(){

        }

    public void render(Graphics g) {
        for(Entity e:entity) {
            e.render(g);
        }

        for(Tile tile:tile) {
            tile.render(g);
        }
    }

    public void tick() {
        for (int i = 0; i < entity.size(); i++) {
            Entity e = entity.get(i);
            e.tick();
        }

        for (int i = 0; i < tile.size(); i++) {
            Tile t = tile.get(i);
            t.tick();
        }

        tile.removeIf(t -> t.removed);
        entity.removeIf(e -> e.removed);
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
    public void createLevel(BufferedImage level)
    {
        int width = level.getWidth();
        int height = level.getHeight();

        for(int y=0; y<height;y++){
            for(int x=0; x<width;x++){
                int pixel = level.getRGB(x,y);

                int red = (pixel >> 16) & 0xFF;
                int green = (pixel >> 8) & 0xFF;
                int blue = (pixel) & 0xFF;

                if(red==0 && green==0 && blue==0) addTile(new Wall(x*64, y*64, 64, 64, true, Id.wall, this));
                if(red==0 && green==0 && blue==255) addEntity(new Player(x*64, y*64, 64, 64,true, Id.player, this));
                if(red==255 && green==0 && blue==0) addEntity(new Mushroom(x*64, y*64, 64, 64, true, Id.mushroom, this));
                if(red==255 && green==119 && blue==0) addEntity(new Goomba(x*64, y*64, 64, 64, true, Id.goomba, this));
                if(red==255 && green==255 && blue==0) addTile(new PowerUpBlock(x*64,y*64, 64, 64, true, Id.powerUp, this, Game.mushroom));
                if(red==0 && (green>123 && green<129) && blue==0) addTile(new Pipe(x*64, y*64, 64, 64*15, true, Id.pipe, this, 128-green));
                if(red==255 && green==250 && blue==0) addTile(new Coin(x*64, y*64, 64, 64, true, Id.coin, this));
            }
        }
    }
    public void clearLevel(){
            entity.clear();
            tile.clear();
    }
}
