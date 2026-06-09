using System;
using System.Drawing;

class Program
{
    static void Main()
    {
        Bitmap img = new Bitmap(@"c:\Users\User\IdeaProjects\Mario\src\main\resources\sheet.png");
        Color c = img.GetPixel(0, 0);
        Console.WriteLine(String.Format("Background Color at 0,0: R={0}, G={1}, B={2}", c.R, c.G, c.B));
    }
}
