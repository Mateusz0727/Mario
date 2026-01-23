package com.mario;

import com.mario.entity.Entity;
import com.mario.gfx.Sprite;
import com.mario.gfx.SpriteSheet;
import com.mario.input.KeyInput;
import com.mario.io.GameData;
import com.mario.io.GameFileManager;

import java.awt.*;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Game extends Canvas implements Runnable {


    public static final int WIDTH = 270;
    public static final int HEIGHT = WIDTH/14*10;
    public static final int SCALE = 4;


    private Thread gameThread;
    private boolean running = false;
    private static BufferedImage image;

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
    public static int COINS_TO_ADVANCE = 10;
    public static int GOOMBAS_TO_ADVANCE = 1;
    public static int STARTING_LIVES = 1;
    public static final String LEVEL_ONE = "/level.png";
    public static final String LEVEL_TWO = "/level2.png";
    public static String currentLevel = LEVEL_ONE;
    public static String pendingLevel = null;
    public static int goombasDefeated = 0;
    public static int levelWidthPixels = WIDTH * SCALE;
    public static int levelHeightPixels = HEIGHT * SCALE;
    private static final GameFileManager fileManager = new GameFileManager();
    private static final String CONFIG_PATH = "data/config.txt";
    private static final String SAVE_PATH = "data/savegame.dat";
    private static final String REPORT_DIR = "reports";
    private static boolean reportWritten = false;

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

        loadConfiguration();
        GameData savedData = loadSavedGame();

        grass = new Sprite(sheet,9,4);
        player = new Sprite(sheet,7,7);
        mushroom = new Sprite(sheet,37,19);
        goomba = new Sprite(sheet,1,2);
        powerUp = new Sprite(sheet,1,4);
        usedPowerUp = new Sprite(sheet,1,9);
        coin =  new Sprite(sheet,8,1);
//        handler.addEntity(new Player(300,200,64,64,true,Id.player,handler));

        if (savedData != null) {
            loadLevel(savedData.getLevel());
            coins = savedData.getCoins();
            goombasDefeated = savedData.getGoombasDefeated();
            lives = savedData.getLives();
        } else {
            loadLevel(LEVEL_ONE);
        }



        //  handler.addTile(new Wall(200,200,64,64,true,Id.wall,handler));
    }


    private synchronized void start() {
        if (running) return; // Prevent multiple threads from starting
        running = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }


    private synchronized void stop() {
        if (!running) return;
        running = false;
        try {
            gameThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void tick() {
        handler.tick();
        if (pendingLevel != null) {
            loadLevel(pendingLevel);
            pendingLevel = null;
        }

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
        if (gameOver && !reportWritten) {
            writeReport("Game over");
            reportWritten = true;
        }
    }


    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3); // Create triple buffering
            return;
        }
        Graphics g = bs.getDrawGraphics();


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


            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println("FPS: " + frames + " | Ticks: " + ticks);
                frames = 0;
                ticks = 0;
            }
        }

        stop();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Game::showMainMenu);
    }

    public static void loadLevel(String levelResource) {
        try{
            BufferedImage level = ImageIO.read(Game.class.getResource(levelResource));
            image = new BufferedImage(level.getWidth(), level.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(level, 0, 0, null);
            g2d.dispose();
            levelWidthPixels = level.getWidth() * 64;
            levelHeightPixels = level.getHeight() * 64;
            currentLevel = levelResource;
            handler.clearLevel();
            handler.createLevel(image);
            coins = 0;
            goombasDefeated = 0;
            showDeathScreen = false;
            deathScreenTime = 0;
            gameOver = false;
            reportWritten = false;
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void requestLevelChange(String levelResource) {
        pendingLevel = levelResource;
    }

    public static void checkForLevelAdvance() {
        if (pendingLevel != null) {
            return;
        }
        if (!LEVEL_ONE.equals(currentLevel)) {
            return;
        }
        if (coins >= COINS_TO_ADVANCE && goombasDefeated >= GOOMBAS_TO_ADVANCE) {
            writeReport("Level completed");
            requestLevelChange(LEVEL_TWO);
        }
    }

    private static void loadConfiguration() {
        try {
            Map<String, String> config = fileManager.loadConfig(CONFIG_PATH);
            COINS_TO_ADVANCE = parseConfigValue(config, "coinsToAdvance", COINS_TO_ADVANCE);
            GOOMBAS_TO_ADVANCE = parseConfigValue(config, "goombasToAdvance", GOOMBAS_TO_ADVANCE);
            STARTING_LIVES = parseConfigValue(config, "startingLives", STARTING_LIVES);
            lives = STARTING_LIVES;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int parseConfigValue(Map<String, String> config, String key, int fallback) {
        String value = config.get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    private static GameData loadSavedGame() {
        try {
            return fileManager.loadGameData(SAVE_PATH);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }



    private static void writeReport(String reason) {
        String reportBody = String.format(
                "Report reason: %s%nLevel: %s%nCoins: %d%nGoombas defeated: %d%nLives: %d%n",
                reason,
                currentLevel,
                coins,
                goombasDefeated,
                lives
        );
        try {
            fileManager.writeReport(REPORT_DIR, reportBody);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void showMainMenu() {
        JFrame frame = new JFrame("Super Mario");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        CardLayout cardLayout = new CardLayout();
        JPanel rootPanel = new JPanel(cardLayout);

        Game game = new Game();
        JPanel gamePanel = new JPanel(new BorderLayout());
        gamePanel.add(game, BorderLayout.CENTER);

        JButton playButton = createMenuButton("Graj");
        JButton exitButton = createMenuButton("Wyjdź");

        MenuPanel menuPanel = new MenuPanel(WIDTH * SCALE, HEIGHT * SCALE);

        playButton.addActionListener(event -> {
            playButton.setEnabled(false);
            menuPanel.stopAnimation();
            cardLayout.show(rootPanel, "game");
            frame.pack();
            frame.setLocationRelativeTo(null);
            game.start();
            game.requestFocusInWindow();
        });

        exitButton.addActionListener(event -> System.exit(0));
        menuPanel.add(Box.createVerticalGlue());
        menuPanel.add(playButton);
        menuPanel.add(Box.createRigidArea(new Dimension(0, 16)));
        menuPanel.add(exitButton);
        menuPanel.add(Box.createVerticalGlue());

        rootPanel.add(menuPanel, "menu");
        rootPanel.add(gamePanel, "game");

        frame.setContentPane(rootPanel);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static JButton createMenuButton(String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("Arial", Font.BOLD, 28));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(255, 140, 0));
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 200, 120), 3, true),
                BorderFactory.createEmptyBorder(12, 24, 12, 24)
        ));
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    private static void drawCloud(Graphics2D g2d, double x, double y, int width, int height) {
        int baseX = (int) x;
        int baseY = (int) y;
        g2d.fillOval(baseX, baseY + height / 4, width, height / 2);
        g2d.fillOval(baseX + width / 10, baseY, width / 2, height / 2);
        g2d.fillOval(baseX + width / 3, baseY - height / 8, width / 2, height / 2);
        g2d.fillOval(baseX + width / 2, baseY + height / 6, width / 2, height / 2);
    }

    private static class MenuPanel extends JPanel {
        private final List<Cloud> clouds = new ArrayList<>();
        private final Timer timer;
        private final int groundHeight = 80;

        private MenuPanel(int width, int height) {
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            setPreferredSize(new Dimension(width, height));
            setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));
            setOpaque(true);

            clouds.add(new Cloud(40f, height * 0.15f, 120, 48, 0.4f));
            clouds.add(new Cloud(width * 0.55f, height * 0.22f, 150, 58, 0.5f));
            clouds.add(new Cloud(width * 0.25f, height * 0.5f, 180, 70, 0.3f));
            clouds.add(new Cloud(width * 0.75f, height * 0.35f, 130, 50, 0.45f));

            timer = new Timer(33, event -> {
                updateClouds();
                repaint();
            });
            timer.start();
        }

        private void updateClouds() {
            int width = getWidth();
            if (width <= 0) {
                return;
            }
            for (Cloud cloud : clouds) {
                cloud.x += cloud.speed;
                if (cloud.x > width + cloud.width) {
                    cloud.x = -cloud.width;
                }
            }
        }

        private void stopAnimation() {
            timer.stop();
        }


        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();

            g2d.setColor(new Color(135, 206, 235));
            g2d.fillRect(0, 0, width, height);

            g2d.setColor(new Color(255, 255, 255, 210));
            for (Cloud cloud : clouds) {
                drawCloud(g2d, cloud.x, cloud.y, cloud.width, cloud.height);
            }

            g2d.setColor(new Color(92, 184, 92));
            g2d.fillRect(0, height - groundHeight, width, groundHeight);

            g2d.dispose();
        }
    }

    private static class Cloud {
        private float x;
        private final float y;
        private final int width;
        private final int height;
        private final float speed;

        private Cloud(float x, float y, int width, int height, float speed) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.speed = speed;
        }
    }
}
