using System;
using System.Drawing;

class Program
{
    static void Main()
    {
        int w = 30;
        int h = 13;
        Bitmap img = new Bitmap(w, h);
        
        Color trans = Color.Transparent;
        Color wall = Color.FromArgb(255, 0, 0, 0); // Black
        Color player = Color.FromArgb(255, 0, 0, 255); // Blue
        Color goomba = Color.FromArgb(255, 255, 0, 0); // Red
        Color pow = Color.FromArgb(255, 255, 255, 0); // Yellow
        Color pipe = Color.FromArgb(255, 0, 255, 0); // Green
        Color trampoline = Color.FromArgb(255, 255, 0, 255); // Magenta
        Color coinGhost = Color.FromArgb(255, 0, 255, 255); // Cyan
        
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                img.SetPixel(x, y, trans);
            }
        }
        
        // Floor
        for (int x = 0; x < w; x++) img.SetPixel(x, 12, wall);
        img.SetPixel(11, 12, trampoline);
        img.SetPixel(18, 12, trampoline);
        
        // Player spawn
        img.SetPixel(15, 11, player);
        
        // Ghost spawn (na wyższej platformie)
        img.SetPixel(15, 5, coinGhost);
        
        // Level 1 platforms
        for (int x = 0; x <= 9; x++) img.SetPixel(x, 9, wall);
        for (int x = 20; x <= 29; x++) img.SetPixel(x, 9, wall);
        img.SetPixel(4, 9, trampoline);
        img.SetPixel(25, 9, trampoline);
        
        // POW Block
        img.SetPixel(14, 9, pow);
        img.SetPixel(15, 9, pow);
        
        // Level 2 platforms
        for (int x = 4; x <= 25; x++) img.SetPixel(x, 6, wall);
        
        // Level 3 platforms
        for (int x = 0; x <= 6; x++) img.SetPixel(x, 3, wall);
        for (int x = 23; x <= 29; x++) img.SetPixel(x, 3, wall);
        
        // Goombas (spawned dynamically now)
        // Removed static goombas
        
        // Add "pipes" at the top corners (green blocks just to make it look right)
        img.SetPixel(0, 2, pipe);
        img.SetPixel(0, 1, pipe);
        img.SetPixel(1, 2, pipe);
        img.SetPixel(1, 1, pipe);
        
        img.SetPixel(28, 2, pipe);
        img.SetPixel(28, 1, pipe);
        img.SetPixel(29, 2, pipe);
        img.SetPixel(29, 1, pipe);
        
        // Save
        img.Save(@"c:\Users\User\IdeaProjects\Mario\src\main\resources\levelmulti.png", System.Drawing.Imaging.ImageFormat.Png);
        Console.WriteLine("levelmulti.png generated!");
    }
}
