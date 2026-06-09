using System;
using System.Drawing;

class Program
{
    static void Main()
    {
        Bitmap img = new Bitmap(@"c:\Users\User\IdeaProjects\Mario\src\main\resources\level.png");
        Console.WriteLine("Width: " + img.Width + ", Height: " + img.Height);
    }
}
