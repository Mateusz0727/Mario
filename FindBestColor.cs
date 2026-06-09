using System;
using System.Drawing;

class Program
{
    static void Main()
    {
        Bitmap img = new Bitmap(@"c:\Users\User\IdeaProjects\Mario\src\main\resources\sheet.png");
        
        int bestRed = 0;
        int bestRX = 0, bestRY = 0;
        
        int bestYellow = 0;
        int bestYX = 0, bestYY = 0;
        
        for (int y = 0; y < 55; y++)
        {
            for (int x = 0; x < 48; x++)
            {
                int px = x * 17 + 1;
                int py = y * 17 + 1;
                
                int r=0, g=0, b=0;
                int redPixels = 0;
                int yellowPixels = 0;
                for (int i=0; i<16; i++) {
                    for (int j=0; j<16; j++) {
                        if (px+i >= img.Width || py+j >= img.Height) continue;
                        Color c = img.GetPixel(px+i, py+j);
                        if (c.R > 220 && c.G < 100 && c.B < 100) redPixels++;
                        if (c.R > 200 && c.G > 180 && c.B < 100) yellowPixels++;
                    }
                }
                
                if (redPixels > bestRed) { bestRed = redPixels; bestRX = x + 1; bestRY = y + 1; }
                if (yellowPixels > bestYellow) { bestYellow = yellowPixels; bestYX = x + 1; bestYY = y + 1; }
            }
        }
        Console.WriteLine(String.Format("Most RED pixels: {0} at ({1}, {2})", bestRed, bestRX, bestRY));
        Console.WriteLine(String.Format("Most YELLOW pixels: {0} at ({1}, {2})", bestYellow, bestYX, bestYY));
    }
}
