using System;
using System.Drawing;

class Program
{
    static void Main()
    {
        Bitmap img = new Bitmap(@"c:\Users\User\IdeaProjects\Mario\src\main\resources\sheet.png");
        
        for (int yOffset = 44; yOffset <= 48; yOffset++)
        {
            Console.WriteLine($"\nTile (1, {yOffset}):");
            int px = 1;
            int py = (yOffset - 1) * 17 + 1;
            
            for (int j = 0; j < 16; j++) {
                for (int i = 0; i < 16; i++) {
                    Color c = img.GetPixel(px + i, py + j);
                    if (c.A < 100) Console.Write(" ");
                    else if (c.R > 200 && c.G < 100 && c.B < 100) Console.Write("R");
                    else if (c.R > 200 && c.G > 200 && c.B < 100) Console.Write("Y");
                    else if (c.R < 100 && c.G < 100 && c.B < 100) Console.Write("#");
                    else if (c.R > 200 && c.G > 200 && c.B > 200) Console.Write(".");
                    else Console.Write("?");
                }
                Console.WriteLine();
            }
        }
    }
}
