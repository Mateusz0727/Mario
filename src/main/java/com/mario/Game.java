package com.mario;

import com.mario.entity.mob.Player;
import com.mario.gfx.Sprite;
import com.mario.gfx.SpriteSheet;
import com.mario.input.KeyInput;
import com.mario.tile.Wall;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * The {@code Game} class represents the main game loop for the Super Mario project.
 * It extends {@link Application} (JavaFX) and uses {@link AnimationTimer}
 * for the game loop running at ~60 FPS.
 */
public class Game extends Application {

    // --- Constants defining the game resolution and scaling ---
    public static final int WIDTH = 270;
    public static final int HEIGHT = WIDTH / 14 * 10;
    public static final int SCALE = 4;

    // --- Level dimensions (used by Player for boundary checks) ---
    public static int levelWidthPixels = WIDTH * SCALE;
    public static int levelHeightPixels = HEIGHT * SCALE;

    // --- Game state ---
    public enum GameState {
        MENU, LOBBY, PLAYING
    }
    public static GameState state = GameState.MENU;
    public static int menuIndex = 0; // 0 = Single Player, 1 = Online Mode

    // --- System Sieciowy ---
    public static com.mario.net.GameClient gameClient;
    public static String lobbyCode = "Oczekiwanie...";
    
    public static void initOnlineClient(boolean create, String code) {
        state = GameState.LOBBY;
        gameClient = new com.mario.net.GameClient("127.0.0.1", 1234);
        gameClient.start();
        if (create) {
            gameClient.sendPacket(com.mario.net.Packet.createRoom());
        } else {
            gameClient.sendPacket(com.mario.net.Packet.joinRoom(code));
        }
    }

    public static int coins = 0;
    public static int goombasDefeated = 0;
    
    // Interpolacja duchów
    private java.util.Map<String, double[]> ghostPositions = new java.util.HashMap<>();
    private int frameCounter = 0;

    public static Handler handler;
    public static SpriteSheet sheet;
    public static Sprite grass;
    public static Sprite player;
    public static Sprite goomba;
    public static Sprite mushroom;
    public static Sprite coin;
    public static Sprite powerUp;
    public static Sprite usedPowerUp;

    private Canvas canvas;
    private GraphicsContext gc;
    private KeyInput keyInput;

    public static void checkForLevelAdvance() {
        // TODO: implement level advance logic
    }

    public static void resetLevel() {
        handler.entity.clear();
        handler.tile.clear();
        coins = 0;
        goombasDefeated = 0;

        javafx.scene.image.Image levelImage = new javafx.scene.image.Image(Game.class.getResourceAsStream("/level.png"));
        handler.createLevel(levelImage);
        
        com.mario.entity.Entity p = handler.findPlayer();
        if (p != null) {
            cam.tick(p);
        }
    }

    public static Camera cam;

    private void initGame() {
        handler = new Handler();
        sheet = new SpriteSheet("/sheet.png");
        cam = new Camera();

        keyInput = new KeyInput();

        grass = new Sprite(sheet, 9, 4);
        player = new Sprite(sheet, 7, 7);
        goomba = new Sprite(sheet, 1, 1); // placeholder coordinates
        mushroom = new Sprite(sheet, 2, 2); // placeholder coordinates
        coin = new Sprite(sheet, 3, 3); // placeholder coordinates
        powerUp = new Sprite(sheet, 4, 4); // placeholder coordinates
        usedPowerUp = new Sprite(sheet, 5, 5); // placeholder coordinates

        javafx.scene.image.Image levelImage = new javafx.scene.image.Image(getClass().getResourceAsStream("/level.png"));
        
        // Update level dimensions based on the actual image size
        levelWidthPixels = (int) levelImage.getWidth() * 64;
        levelHeightPixels = (int) levelImage.getHeight() * 64;

        handler.createLevel(levelImage);
    }

    /**
     * Updates the game logic.
     */
    public void tick() {
        if (state == GameState.PLAYING) {
            handler.tick();
            
            // Update camera position to follow player
            com.mario.entity.Entity p = handler.findPlayer();
            if (p != null) {
                cam.tick(p);
                
                // System wymiany danych sieciowych (Wysłanie ułamka klatki w świat)
                if (gameClient != null && gameClient.connected) {
                    Player localPlayer = (Player) p;
                    int scaleState = localPlayer.state == com.mario.states.PlayerState.BIG ? 1 : 0;
                    
                    // Budowa paczki o sobie samym
                    com.mario.net.GameData data = new com.mario.net.GameData(
                        "Player1", p.getX(), p.getY(), p.jumping, p.falling, scaleState, 0
                    ); 
                    
                    // Wyrzucenie fizyki przez lejek OOS TCP
                    gameClient.sendPacket(com.mario.net.Packet.update(lobbyCode, data));
                }
            }
        }
    }

    /**
     * Renders all game graphics using the JavaFX GraphicsContext.
     */
    public void render() {
        if (state == GameState.MENU) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 60));
            gc.fillText("SUPER MARIO", canvas.getWidth() / 2 - 200, 200);

            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 40));
            
            gc.setFill(menuIndex == 0 ? Color.YELLOW : Color.WHITE);
            gc.fillText("Single Player", canvas.getWidth() / 2 - 120, 400);
            
            gc.setFill(menuIndex == 1 ? Color.YELLOW : Color.WHITE);
            gc.fillText("Online Mode", canvas.getWidth() / 2 - 120, 500);

        } else if (state == GameState.LOBBY) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
            
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 40));
            gc.fillText("POCZEKALNIA SIECIOWA", canvas.getWidth() / 2 - 250, 200);

            if (lobbyCode.length() == 4) {
                gc.setFill(Color.YELLOW);
                gc.fillText("KOD LOBBY: " + lobbyCode, canvas.getWidth() / 2 - 180, 300);
                gc.setFill(Color.WHITE);
                gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.NORMAL, 20));
                gc.fillText("Poczekaj aż kolega użyje u siebie przycisku Dołącz...", canvas.getWidth() / 2 - 230, 400);
            } else {
                gc.fillText(lobbyCode, canvas.getWidth() / 2 - 150, 300); // Często = Oczekiwanie...
            }

        } else if (state == GameState.PLAYING) {
            // --- Clear background ---
            gc.setFill(Color.CYAN);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            // Shift rendering context by camera offset
            gc.translate(-cam.getX(), -cam.getY());
            
            handler.render(gc);
            
            // ===== RYSOWANIE DUCHÓW INNYCH GRACZY =====
            if (gameClient != null && gameClient.connected) {
                gc.setGlobalAlpha(0.6); 
                
                for (com.mario.net.GameData targetGhost : gameClient.ghosts.values()) {
                    String id = targetGhost.playerId;
                    
                    // Inicjalizacja lub pobranie pozycji interpolowanej
                    double[] currentPos = ghostPositions.get(id);
                    if (currentPos == null) {
                        currentPos = new double[]{targetGhost.x, targetGhost.y};
                        ghostPositions.put(id, currentPos);
                    }
                    
                    // LERP - Płynne "dopływanie" ducha do celu (0.1 = siła wygładzania)
                    currentPos[0] += (targetGhost.x - currentPos[0]) * 0.15;
                    currentPos[1] += (targetGhost.y - currentPos[1]) * 0.15;

                    int ghostW = targetGhost.state == 1 ? 128 : 64;
                    int ghostH = targetGhost.state == 1 ? 128 : 64;
                    
                    // Prosta animacja chodzenia dla duchów (oparta na ruchu)
                    Image ghostImg = player.getImage(); // Fallback
                    // Tu można by dodać wybór klatki dla ducha
                    
                    gc.drawImage(ghostImg, currentPos[0], currentPos[1], ghostW, ghostH);
                }
                
                gc.setGlobalAlpha(1.0); 
            }
            // ===========================================

            // Restore rendering context for static UI elements
            gc.translate(cam.getX(), cam.getY());

            // Draw Score (Coins)
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 40));
            gc.fillText("Coins: " + coins, 20, 50);
        }
    }

    @Override
    public void start(Stage stage) {
        initGame();

        // Restore fixed window size 1080x768 approx
        canvas = new Canvas(WIDTH * SCALE, HEIGHT * SCALE);
        gc = canvas.getGraphicsContext2D();

        Pane root = new Pane(canvas);
        Scene scene = new Scene(root, WIDTH * SCALE, HEIGHT * SCALE);

        // --- Key input via JavaFX Scene ---
        scene.setOnKeyPressed(e -> keyInput.handleKeyPressed(e));
        scene.setOnKeyReleased(e -> keyInput.handleKeyReleased(e));

        stage.setTitle("Super Mario");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        // --- Game loop using AnimationTimer (~60 FPS) ---
        new AnimationTimer() {
            private long lastTime = System.nanoTime();
            private long timer = System.currentTimeMillis();
            private double delta = 0;
            private final double ns = 1_000_000_000.0 / 60.0;
            private int frames = 0;
            private int ticks = 0;

            @Override
            public void handle(long now) {
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
        }.start();
    }

    /**
     * The entry point of the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
