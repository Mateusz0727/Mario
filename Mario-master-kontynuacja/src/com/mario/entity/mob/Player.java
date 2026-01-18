package com.mario.entity.mob;

import com.mario.Game;
import com.mario.Handler;
import com.mario.Id;
import com.mario.entity.Entity;
import com.mario.states.PlayerState;
import com.mario.tile.Tile;

import java.awt.*;

public class Player extends Entity {

    private PlayerState state;

    private int pixelsTravelled = 0;

    public Player(int x, int y, int width, int height, boolean solid, Id id, Handler handler) {
        super(x, y, width, height, solid, id, handler);
        state = PlayerState.SMALL;
    }

    @Override
    public void render(Graphics g) {
       g.drawImage(Game.player.getBufferImage(),x,y,width,height,null );
    }

    @Override
    public void tick() {
        x+=velX;
        y+=velY;

        if(goingDownPipe){
            pixelsTravelled+=velY;
        }

        if(x<=0)x=0;
        if(y<=0)y=0;
        if(x+width>=1080) x=1080-width;
        if(y+height>=771) y=771-height;

        for(Tile t:handler.tile){
            if (!t.solid && !goingDownPipe) continue;
            if (t.getId()==Id.wall)
            {
                if(getBoundsTop().intersects(t.getBounds()))
                {
                    setVelY(0);
                    if(jumping) {
                        jumping = false;
                        gravity=0.0;
                        falling=true;
                    }
                    if(t.getId()==Id.powerUp){
                        if(getBoundsTop().intersects(t.getBounds())) t.activated = true;
                    }
                    y=t.getY()+t.height;
                }
                if(getBoundsBottom().intersects(t.getBounds()))
                {
                    setVelY(0);
                    if(falling) falling=false;
                }
                else {
                    if(!falling&&!jumping){ falling=true;
                    gravity=0.0;}
                }
                if(getBoundsLeft().intersects(t.getBounds())) {
                    setVelX(0);
                    x = t.getX() + t.width;
                }
                if(getBoundsRight().intersects(t.getBounds())) {
                    setVelX(0);
                    x = t.getX() - width;
                }
            }
            if(getBounds().intersects(t.getBounds()) && t.getId()==Id.coin) {
                Game.coins++;
                t.removed = true;
            }
        }

        for(int i=0;i<handler.entity.size();i++){
            Entity e=handler.entity.get(i);

            if(e.getId()==Id.mushroom){
                if(getBounds().intersects(e.getBounds())){
                    int tpX = getX();
                    int tpY = getY();
                    width*=2;
                    height*=2;
                    setX(tpX-width);
                    setY(tpY-height);
                    if(state == PlayerState.SMALL) state =  PlayerState.BIG;
                    e.die();
                }
            } else if(e.getId()==Id.goomba){
                if(getBoundsBottom().intersects(e.getBoundsTop())){
                    e.die();
                }
                else if(getBounds().intersects(e.getBounds())){
                    if(state == PlayerState.BIG) {
                        state = PlayerState.SMALL;
                        width/=2;
                        height/=2;
                        x+=width;
                        y+=height;
                    }
                    else if(state == PlayerState.SMALL) die();
                }
            }
        }

        if(jumping && !goingDownPipe)
        {
            gravity-=0.5;
            setVelY((int)-gravity);
            if(gravity<=0.0)
            {
                jumping=false;
                falling=true;
            }
        }
        if(falling && !goingDownPipe)
        {
            gravity+=0.5;
            setVelY((int)gravity);
        }
        if(goingDownPipe){
            for(int i=0;i<Game.handler.tile.size();i++){
                Tile t = Game.handler.tile.get(i);
                if(t.getId()==Id.pipe){
                    if(getBoundsTop().intersects(t.getBounds())) {
                        switch (t.facing) {
                            case 0:
                                setVelY(-5);
                                setVelX(0);
                                break;
                            case 2:
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
