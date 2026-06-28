package com.marioserver;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class ServerMapLoader {
    
    public static List<ServerPlatform> loadPlatforms(String resourcePath) {
        List<ServerPlatform> platforms = new ArrayList<>();
        try {
            InputStream is = ServerMapLoader.class.getResourceAsStream(resourcePath);
            if (is == null) {
                System.err.println("Could not find map image: " + resourcePath);
                return platforms;
            }
            
            BufferedImage img = ImageIO.read(is);
            int width = img.getWidth();
            int height = img.getHeight();
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int pixel = img.getRGB(x, y);
                    int a = (pixel >> 24) & 0xff;
                    int r = (pixel >> 16) & 0xff;
                    int g = (pixel >> 8) & 0xff;
                    int b = (pixel) & 0xff;
                    
                    if (a == 0) continue;
                    

                    if (r == 0 && g == 0 && b == 0) { // Wall
                        platforms.add(new ServerPlatform(x * 64, (x + 1) * 64, y * 64));
                    } else if (r == 255 && g == 255 && b == 0) { // PowerUpBlock
                        platforms.add(new ServerPlatform(x * 64, (x + 1) * 64, y * 64));
                    } else if (r == 255 && g == 0 && b == 255) { // Trampoline
                        platforms.add(new ServerPlatform(x * 64, (x + 1) * 64, y * 64));
                    } else if (r == 0 && g == 255 && b == 0) { // Pipe
                        platforms.add(new ServerPlatform(x * 64, x * 64 + 192, y * 64));
                    } else if (r == 0 && g == 100 && b == 0 || r == 0 && g == 101 && b == 0) { // CloudPipe
                        platforms.add(new ServerPlatform(x * 64, x * 64 + 128, y * 64));
                    }
                }
            }
            System.out.println("Loaded " + platforms.size() + " platforms from " + resourcePath);
            for (ServerPlatform p : platforms) {
                if (p.y >= 11 * 64) {
                    System.out.println("Platform at y=" + p.y + " from x1=" + p.x1 + " to x2=" + p.x2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return platforms;
    }
}
