package com.marioserver;

public class ServerGoomba {
    public int id;
    public double x, y;
    public double velX, velY;
    public boolean dying = false;
    public boolean dead = false;
    
    public static final double WIDTH = 64;
    public static final double HEIGHT = 64;
    
    static class Platform {
        double x1, x2, y;
        public Platform(double x1, double x2, double y) {
            this.x1 = x1; this.x2 = x2; this.y = y;
        }
    }
    
    // Zbliżony układ mapy "levelmulti.png" (Map size: 30x13)
    static Platform[] platforms = new Platform[] {
        new Platform(0, 7*64, 3*64),       // Górne lewe
        new Platform(23*64, 30*64, 3*64),  // Górne prawe
        new Platform(4*64, 26*64, 6*64),   // Środkowa długa
        new Platform(0, 10*64, 9*64),      // Dolne lewe
        new Platform(20*64, 30*64, 9*64),  // Dolne prawe
        new Platform(0, 30*64, 12*64)      // Podłoga (Y = 768)
    };

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
            for (Platform p : platforms) {
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
