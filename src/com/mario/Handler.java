package com.mario;

import com.mario.entity.Entity;
import com.mario.tile.Tile;
import com.mario.tile.Wall;

import java.awt.*;
import java.util.LinkedList;

public class Handler {
    public LinkedList<Entity> entity = new LinkedList<Entity>();
    public LinkedList<Tile> tile = new LinkedList<Tile>();

        public Handler(){
            createLevel();
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
        for(Entity e:entity) {
            e.tick();
        }
        for(Tile tile:tile) {
            tile.tick();
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
    public void createLevel()
    {
        for(int i=0;i<Game.WIDTH*Game.SCALE/64;i++)
        {
            addTile(new Wall(i*64,Game.HEIGHT*Game.SCALE-64,64,64,true,Id.wall,this));
            if(i!=0&&i!=1&&i!=15&&i!=16) addTile(new Wall(i*64,300,64,64,true,Id.wall,this));
        }
    }
}
