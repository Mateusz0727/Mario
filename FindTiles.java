import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class FindTiles {
    public static void main(String[] args) throws Exception {
        BufferedImage img = ImageIO.read(new File("src/main/resources/sheet.png"));
        int width = img.getWidth();
        int height = img.getHeight();
        System.out.println("Image size: " + width + "x" + height);
        
        int cols = (width + 1) / 17;
        int rows = (height + 1) / 17;
        System.out.println("Cols: " + cols + ", Rows: " + rows);
    }
}
