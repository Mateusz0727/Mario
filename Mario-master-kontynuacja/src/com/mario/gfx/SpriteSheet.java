package com.mario.gfx;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class SpriteSheet {
    private BufferedImage sheet;
    public SpriteSheet(String path) {
        try {
            sheet = ImageIO.read(getClass().getResource(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public BufferedImage getSprite(int x,int y) {
        return sheet.getSubimage(x*16-16+x,y*16-16+y,16,16);
    }
}
