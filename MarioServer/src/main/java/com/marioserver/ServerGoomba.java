package com.marioserver;

public class ServerGoomba {
    public int id;
    public double x, y;
    public double velX, velY;
    public boolean dying = false;
    public boolean dead = false;
    
    public static final double WIDTH = 64;
    public static final double HEIGHT = 64;
    


    public ServerGoomba(int id, double startX, double startY) {
        this.id = id;
        this.x = startX;
        this.y = startY;
        
        if (x < 15 * 64) {
            velX = 2;
        } else {
            velX = -2;
        }
    }
    
    public void tick() {
        if (dead) return;
        
        if (dying) {
            velY += 0.5; // gravity
            x += velX;
            y += velY;
            if (y > 15 * 64) {
                dead = true;
            }
            return;
        }
        
        // Grawitacja i ruch
        velY += 0.5;
        if (velY > 15) velY = 15;
        
        double nextY = y + velY;
        boolean onGround = false;
        
        // Kolizja z platformami z góry (opadanie)
        if (velY > 0) {
            for (ServerPlatform p : Server.platforms) {
                // Sprawdź czy Goomba jest w osi X nad platformą
                if (x + WIDTH > p.x1 && x < p.x2) {
                    // Sprawdź czy przecięła linię platformy
                    if (y + HEIGHT <= p.y && nextY + HEIGHT >= p.y) {
                        nextY = p.y - HEIGHT;
                        velY = 0;
                        onGround = true;
                        break;
                    }
                }
            }
        }
        y = nextY;
        x += velX;
        
        // Odbijanie od granic ekranu
        if (x <= 0) {
            x = 0;
            velX = -velX;
        }
        if (x + WIDTH >= 30 * 64) {
            x = 30 * 64 - WIDTH;
            velX = -velX;
        }
        
        // Jeśli Goomba wypadnie za mapę
        if (y > 16 * 64) {
            dead = true;
        }
    }
    
    public void die() {
        if (!dying) {
            dying = true;
            velX = 0;
            velY = -5; // Pop up
        }
    }
}
