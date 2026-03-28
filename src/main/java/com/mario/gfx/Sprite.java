package com.mario.gfx;

import javafx.scene.image.Image;

public class Sprite {
    public SpriteSheet sheet;
    public Image image;

    public Sprite(SpriteSheet sheet, int x, int y) {
        image = sheet.getSprite(x, y);
    }

    public Image getImage() {
        return image;
    }
}
