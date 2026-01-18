package com.mario;

import com.mario.entity.Entity;
import com.mario.gfx.Sprite;
import com.mario.gfx.SpriteSheet;
import com.mario.input.KeyInput;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;

/**
 * The {@code Game} class represents the main game loop for the Super Mario project.
 * It extends {@link Canvas} (used for rendering graphics) and implements {@link Runnable}
 * so that the game can run in a separate thread.
 */
public class Game extends Canvas implements Runnable {

    // --- Constants defining the game resolution and scaling ---
    public static final int WIDTH = 270;
    public static final int HEIGHT = WIDTH/14*10;
    public static final int SCALE = 4;

    // --- Thread control variables ---
    private Thread gameThread;
    private boolean running = false;
    private BufferedImage image;

    public static int coins = 0;
    public static int lives = 1;
    public static int deathScreenTime = 0;
    public static boolean showDeathScreen = true;
    public static boolean gameOver = false;

    public static Handler handler;
    public static SpriteSheet sheet;
    public static Sprite grass;
    public static Sprite player;
    public static Camera cam;
    public static Sprite mushroom;
    public static Sprite goomba;
    public static Sprite powerUp;
    public static Sprite usedPowerUp;
    public static Sprite coin;


    /**
     * Constructs a new {@code Game} instance.
     * Sets up the preferred, maximum, and minimum window dimensions
     * according to the defined resolution and scale.
     */
    public Game() {
        Dimension screenSize = new Dimension(WIDTH * SCALE, HEIGHT * SCALE);
        setPreferredSize(screenSize);
        setMaximumSize(screenSize);
        setMinimumSize(screenSize);
    }

    private void init() {
        handler = new Handler();
        sheet = new SpriteSheet("/sheet.png");
        cam =  new Camera();

        addKeyListener(new KeyInput());

        grass = new Sprite(sheet,9,4);
        player = new Sprite(sheet,7,7);
        mushroom = new Sprite(sheet,37,19);
        goomba = new Sprite(sheet,1,2);
        powerUp = new Sprite(sheet,1,4);
        usedPowerUp = new Sprite(sheet,1,9);
        coin =  new Sprite(sheet,8,1);
//        handler.addEntity(new Player(300,200,64,64,true,Id.player,handler));

        try{
            BufferedImage level = ImageIO.read(getClass().getResource("/level.png"));
             image = new BufferedImage(level.getWidth(), level.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(level, 0, 0, null);
            g2d.dispose();
        }catch(IOException e){
            e.printStackTrace();
        }



        //  handler.addTile(new Wall(200,200,64,64,true,Id.wall,handler));
    }

    /**
     * Starts the game thread if it is not already running.
     * This method is called from the {@code main()} method.
     */
    private synchronized void start() {
        if (running) return; // Prevent multiple threads from starting
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }

    /**
     * Stops the game loop and waits for the game thread to terminate.
     */
    private synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Updates the game logic.
     * This method will be used for player movement, physics, collisions, etc.
     */
    public void tick() {
        handler.tick();

        for(Entity e:handler.entity){
            if(e.getId()==Id.player){
                if(!e.goingDownPipe) cam.tick(e);
            }
        }
        if(showDeathScreen && !gameOver) deathScreenTime++;
        if(deathScreenTime >= 180) {

            showDeathScreen = false;
            deathScreenTime = 0;
            handler.clearLevel();
            handler.createLevel(image);
        }
    }

    /**
     * Renders all game graphics.
     * This method is responsible for drawing the background, player, enemies, and UI.
     * It uses {@link BufferStrategy} to avoid flickering and ensure smooth rendering.
     */
    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3); // Create triple buffering
            return;
        }
        Graphics g = bs.getDrawGraphics();

        // --- Example background rendering ---
        g.setColor(Color.CYAN);
        g.fillRect(0, 0, getWidth(), getHeight());
        if(!showDeathScreen){
            g.drawImage(Game.coin.getBufferImage(),20, 20, 75, 75, null);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Courier", Font.BOLD, 20));
            g.drawString("x" + coins, 100, 95);
        }
        if(showDeathScreen){
            if(!gameOver){
                g.setColor(Color.WHITE);
                g.setFont(new Font("Courier", Font.BOLD, 50));
                g.drawImage(Game.player.getBufferImage(),500,300,100,100,null);
                g.drawString("lives:" + lives, 610, 400);
            }else{
                g.setColor(Color.WHITE);
                g.setFont(new Font("Courier", Font.BOLD, 50));
                g.drawString("Game over", 610, 400);
            }
        }
        g.translate(-cam.getX(), -cam.getY());
        if(!showDeathScreen) handler.render(g);
        g.dispose();
        bs.show();
    }

    public int getFrameWidth(){
        return WIDTH*SCALE;
    }

    public int getFrameHeight(){
        return HEIGHT*SCALE;
    }

    /**
     * The main game loop.
     * Runs at a fixed rate of 60 updates per second (60 ticks per second),
     * updating the game logic and rendering the scene.
     */
    @Override
    public void run() {
        init();
        requestFocus();
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        double delta = 0;
        double ns = 1000000000.0 / 60.0;
        int frames = 0;
        int ticks = 0;

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                tick();
                ticks++;
                delta--;
            }

            render();
            frames++;

            // Display FPS and tick count once per second (for debugging)
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames + " | Ticks: " + ticks);
                frames = 0;
                ticks = 0;
            }
        }

        stop();
    }

    /**
     * The entry point of the application.
     * Creates a window using {@link JFrame}, attaches the {@code Game} canvas,
     * and starts the game loop.
     */
    public static void main(String[] args) {
        Game game = new Game();

        JFrame frame = new JFrame("Super Mario");
        frame.add(game);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null); // Center the window
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        game.start();
    }
}
