package com.mario.gfx;

import com.mario.Handler;
import com.mario.Id;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Sprite {
    public static Sprite[] player;
    public SpriteSheet sheet;
    public BufferedImage image;
    public Sprite(SpriteSheet sheet,int x,int y) {
        image=sheet.getSprite(x,y);
    }

    public Sprite(int x, int y, int width, int height, boolean solid, Id id, Handler handler) {
    }

    public BufferedImage getBufferImage() {
        return image;
    }


}
