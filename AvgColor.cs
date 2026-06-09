using System;
using System.Drawing;

class Program
{
    static void Main()
    {
        Bitmap img = new Bitmap(@"c:\Users\User\IdeaProjects\Mario\src\main\resources\sheet.png");
        
        for (int y = 42; y <= 48; y++)
        {
            for (int x = 0; x < 3; x++)
            {
                int px = x * 17 + 1;
                int py = y * 17 + 1;
                
                int r=0, g=0, b=0;
                for (int i=0; i<16; i++) {
                    for (int j=0; j<16; j++) {
                        Color c = img.GetPixel(px+i, py+j);
                        if (c.A > 0) {
                            r += c.R;
                            g += c.G;
                            b += c.B;
                        }
                    }
                }
                r /= 256; g /= 256; b /= 256;
                Console.WriteLine(String.Format("Tile ({0}, {1}) Avg RGB: {2}, {3}, {4}", x+1, y+1, r, g, b));
            }
        }
    }
}
