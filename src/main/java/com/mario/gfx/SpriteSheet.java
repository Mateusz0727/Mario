package com.mario.gfx;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;

public class SpriteSheet {
    private Image sheet;

    public SpriteSheet(String path) {
        sheet = new Image(getClass().getResourceAsStream(path));
    }

    public Image getSprite(int x, int y) {
        PixelReader reader = sheet.getPixelReader();
        return new WritableImage(reader, x * 16 - 16 + x, y * 16 - 16 + y, 16, 16);
    }
}
