import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class MapReader {
    public static void main(String[] args) throws Exception {
        File file = new File("src/main/resources/levelmulti.png");
        if (!file.exists()) {
            System.out.println("File not found!");
            return;
        }
        BufferedImage image = ImageIO.read(file);
        int width = image.getWidth();
        int height = image.getHeight();
        
        System.out.println("Map size: " + width + "x" + height);
        
        for (int y = 0; y < height; y++) {
            System.out.print(y + ": ");
            for (int x = 0; x < width; x++) {
                int pixel = image.getRGB(x, y);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;
                int alpha = (pixel >> 24) & 0xff;
                
                if (alpha == 0) {
                    System.out.print(".");
                } else if (red == 0 && green == 0 && blue == 0) {
                    System.out.print("B"); // Wall
                } else if (red == 0 && green == 255 && blue == 0) {
                    System.out.print("P"); // Pipe
                } else {
                    System.out.print("X"); // Other
                }
            }
            System.out.println();
        }
    }
}
