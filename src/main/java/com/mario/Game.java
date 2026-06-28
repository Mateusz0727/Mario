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
 * The {@code Game} class represents the main game loop for the Super Mario
 * project.
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
        MENU, LOBBY, PLAYING, GAME_OVER
    }

    public static GameState state = GameState.MENU;
    public static int menuIndex = 0; // 0 = Single Player, 1 = Online Mode
    public static int currentLevel = 1;
    public static String playerName = "Player1";

    // --- System Sieciowy ---
    public static String SERVER_IP = "127.0.0.1";
    public static com.mario.net.GameClient gameClient;
    public static String lobbyCode = "Oczekiwanie...";

    public static void initOnlineClient(boolean create, String code) {
        state = GameState.LOBBY;
        gameClient = new com.mario.net.GameClient(SERVER_IP, 1234);
        gameClient.start();
        if (create) {
            gameClient.sendPacket(com.mario.net.Packet.createRoom());
        } else {
            gameClient.sendPacket(com.mario.net.Packet.joinRoom(code));
        }
    }

    public static int coins = 0;
    public static int goombasDefeated = 0;
    
    public static int lastFPS = 0;
    public static int ping = 0;
    public static long lastPingSentTime = 0;

    // Interpolacja duchów
    private java.util.Map<String, double[]> ghostPositions = new java.util.HashMap<>();
    private int frameCounter = 0;

    public static Handler handler;
    public static SpriteSheet sheet;
    public static Sprite grass;
    public static Sprite player;
    public static Sprite goomba;
    public static Sprite mushroom;
    public static Sprite[] coinAnim = new Sprite[4];
    public static Sprite coinGhostSprite;
    public static Sprite powerUp;
    public static Sprite usedPowerUp;

    public static Sprite[] pipeTop = new Sprite[2];
    public static Sprite[] pipeBody = new Sprite[2];

    public static Sprite[] bigPipeTop = new Sprite[3];
    public static Sprite[] bigPipeBody = new Sprite[3];

    public static javafx.scene.image.Image mushroomImage;

    private Canvas canvas;
    private GraphicsContext gc;
    private KeyInput keyInput;

    public static boolean levelPendingAdvance = false;

    // Spawner Goombas
    public static java.util.Random randomSpawner = new java.util.Random(1337);
    public static int spawnerTickCount = 0;
    public static int nextSpawnTime = 180;

    public static void checkForLevelAdvance() {
        if (menuIndex == 1)
            return; // W trybie multiplayer nie kończymy poziomu przez zebranie monet!

        boolean hasCoins = false;
        for (com.mario.tile.Tile t : handler.tile) {
            if (t.getId() == Id.coin && !t.removed) {
                hasCoins = true;
                break;
            }
        }
        if (!hasCoins) {
            levelPendingAdvance = true;
        }
    }

    public static void resetLevel() {
        handler.entity.clear();
        handler.tile.clear();
        coins = 0;
        goombasDefeated = 0;

        randomSpawner = new java.util.Random(1337);
        spawnerTickCount = 0;
        nextSpawnTime = randomSpawner.nextInt(300) + 180;

        String levelPath = "/level.png";
        if (menuIndex == 1) { // Online Mode
            levelPath = "/levelmulti.png";
        } else if (currentLevel == 2) {
            levelPath = "/level2.png";
        }

        javafx.scene.image.Image levelImage = new javafx.scene.image.Image(
                Game.class.getResourceAsStream(levelPath));

        levelWidthPixels = (int) levelImage.getWidth() * 64;
        levelHeightPixels = (int) levelImage.getHeight() * 64;

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

        grass = new Sprite(sheet, 2, 1); // Cegla
        player = new Sprite(sheet, 7, 7);
        goomba = new Sprite(sheet, 1, 1); // placeholder coordinates
        mushroom = new Sprite(sheet, 2, 2); // loaded from mushroom.png
        for (int i = 0; i < 4; i++) {
            coinAnim[i] = new Sprite(sheet, i + 1, 36);
        }
        coinGhostSprite = new Sprite(sheet, 7, 7);
        powerUp = new Sprite(sheet, 3, 1); // Question Block
        usedPowerUp = new Sprite(sheet, 20, 1); // Used Block

        for (int i = 0; i < 2; i++) {
            pipeTop[i] = new Sprite(sheet, 15 + i, 1);
            pipeBody[i] = new Sprite(sheet, 15 + i, 2);
        }

        for (int i = 0; i < 3; i++) {
            bigPipeTop[i] = new Sprite(sheet, 12 + i, 1);
            bigPipeBody[i] = new Sprite(sheet, 12 + i, 2);
        }

        javafx.scene.image.Image levelImage = new javafx.scene.image.Image(
                getClass().getResourceAsStream("/level.png"));

        mushroomImage = new javafx.scene.image.Image(getClass().getResourceAsStream("/mushroom.png"));

        // Update level dimensions based on the actual image size
        levelWidthPixels = (int) levelImage.getWidth() * 64;
        levelHeightPixels = (int) levelImage.getHeight() * 64;

        handler.createLevel(levelImage);
    }

    public static String authenticate(String username, String password, boolean isRegister) {
        try {
            java.net.Socket s = new java.net.Socket(SERVER_IP, 1234);
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream());

            if (isRegister) {
                out.writeObject(com.mario.net.Packet.register(username, password));
            } else {
                out.writeObject(com.mario.net.Packet.login(username, password));
            }
            out.flush();

            Object obj = in.readObject();
            s.close();

            if (obj instanceof com.mario.net.Packet) {
                com.mario.net.Packet p = (com.mario.net.Packet) obj;
                if (p.type == com.mario.net.Packet.Type.LOGIN_RESPONSE
                        || p.type == com.mario.net.Packet.Type.REGISTER_RESPONSE) {
                    if ("SUCCESS".equals(p.roomCode)) {
                        return "SUCCESS";
                    } else {
                        return p.message;
                    }
                }
            }
            return "Nieznany błąd serwera.";
        } catch (Exception e) {
            return "Brak połączenia z serwerem.";
        }
    }

    public static void fetchPlayerStats() {
        try {
            System.out.println("Łączenie z serwerem w celu pobrania statystyk dla: " + playerName + "...");
            java.net.Socket s = new java.net.Socket(SERVER_IP, 1234);
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(s.getInputStream());

            out.writeObject(com.mario.net.Packet.loadDb(playerName));
            out.flush();

            Object obj = in.readObject();
            if (obj instanceof com.mario.net.Packet) {
                com.mario.net.Packet p = (com.mario.net.Packet) obj;
                if (p.type == com.mario.net.Packet.Type.LOAD_DB_RESPONSE && p.message != null) {
                    String[] parts = p.message.split(";");
                    if (parts.length >= 4) {
                        coins = Integer.parseInt(parts[0]);
                        goombasDefeated = Integer.parseInt(parts[1]);
                        System.out.println("Wczytano statystyki z serwera dla " + playerName + ". Monety: " + coins);
                    }
                }
            }
            s.close();
        } catch (Exception e) {
            System.out.println("Nie udało się połączyć z serwerem bazy danych. Start od 0.");
        }
    }

    /**
     * Updates the game logic.
     */
    public void tick() {
        if (state == GameState.PLAYING) {
            handler.tick();

            if (levelPendingAdvance) {
                levelPendingAdvance = false;
                currentLevel++;
                if (currentLevel > 2) {
                    System.out.println("Gratulacje! Ukończyłeś wszystkie poziomy.");
                    currentLevel = 1;
                    state = GameState.MENU;
                } else {
                    resetLevel();
                }
            }

            // Goomba Spawner dla mapy multiplayer
            if (menuIndex == 1) {
                spawnerTickCount++;
                if (spawnerTickCount >= nextSpawnTime) {
                    spawnerTickCount = 0;
                    // Losowy czas 3-8 sekund (180 - 480 klatek przy 60 FPS)
                    nextSpawnTime = randomSpawner.nextInt(300) + 180;

                    int pipeSide = randomSpawner.nextInt(2); // 0 = lewa rura, 1 = prawa rura
                    int spawnX = (pipeSide == 0) ? (3 * 64) : (26 * 64);
                    int spawnY = 2 * 64; // Wysokość rury

                    handler.addEntity(
                            new com.mario.entity.mob.Goomba(spawnX, spawnY, 64, 64, true, Id.goomba, handler));
                }
            }

            // Update camera position to follow player
            com.mario.entity.Entity p = handler.findPlayer();
            if (p != null) {
                cam.tick(p);

                // System wymiany danych sieciowych (Wysłanie ułamka klatki w świat)
                if (gameClient != null && gameClient.connected) {
                    Player localPlayer = (Player) p;
                    int scaleState = localPlayer.state == com.mario.states.PlayerState.BIG ? 1 : 0;

                    com.mario.net.GameData data = new com.mario.net.GameData(
                            playerName, p.getX(), p.getY(), p.jumping, p.falling, scaleState,
                            localPlayer.facingRight ? 1 : 0);

                    // Wyrzucenie fizyki przez lejek OOS TCP
                    gameClient.sendPacket(com.mario.net.Packet.update(lobbyCode, data));
                }
            } else if (gameClient != null && gameClient.connected) {
                if (gameClient.ghosts.isEmpty()) {
                    Game.state = GameState.GAME_OVER;
                } else {
                    // Kamera podąża za duchem, jeśli nie żyjemy (Tryb obserwatora)
                    com.mario.net.GameData targetGhost = gameClient.ghosts.values().iterator().next();
                    if (targetGhost.state == 2) {
                        // Both players are dead
                        Game.state = GameState.GAME_OVER;
                    } else {
                        int ghostW = 48;
                        int ghostH = targetGhost.state == 1 ? 96 : 48;
                        cam.tick(targetGhost.x, targetGhost.y, ghostW, ghostH);
                    }
                }
            }
            frameCounter++;
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

            gc.save(); // Save original untransformed context

            if (menuIndex == 1) { // Online Mode - dopasuj całą mapę do ekranu
                double scaleX = canvas.getWidth() / (double) levelWidthPixels;
                double scaleY = canvas.getHeight() / (double) levelHeightPixels;
                double fitScale = Math.min(scaleX, scaleY);

                double offsetX = (canvas.getWidth() - (levelWidthPixels * fitScale)) / 2.0;
                double offsetY = (canvas.getHeight() - (levelHeightPixels * fitScale)) / 2.0;

                gc.translate(offsetX, offsetY);
                gc.scale(fitScale, fitScale);
            } else {
                // Shift rendering context by camera offset (Single Player)
                gc.translate(-cam.getX(), -cam.getY());
            }

            handler.render(gc);

            // ===== RYSOWANIE DUCHÓW INNYCH GRACZY =====
            if (gameClient != null && gameClient.connected) {
                gc.setGlobalAlpha(0.6);

                for (com.mario.net.GameData targetGhost : gameClient.ghosts.values()) {
                    if (targetGhost.state == 2)
                        continue; // Nie rysuj martwego gracza

                    String id = targetGhost.playerId;

                    // Inicjalizacja lub pobranie pozycji interpolowanej
                    double[] currentPos = ghostPositions.get(id);
                    if (currentPos == null) {
                        currentPos = new double[] { targetGhost.x, targetGhost.y };
                        ghostPositions.put(id, currentPos);
                    }

                    boolean moving = Math.abs(targetGhost.x - currentPos[0]) > 0.5;

                    // LERP - Płynne "dopływanie" ducha do celu (0.1 = siła wygładzania)
                    currentPos[0] += (targetGhost.x - currentPos[0]) * 0.15;
                    currentPos[1] += (targetGhost.y - currentPos[1]) * 0.15;

                    int ghostW = 48; // Było 64 lub 128
                    int ghostH = targetGhost.state == 1 ? 96 : 48; // Było 64 lub 128

                    Image ghostImg = com.mario.entity.mob.Player.getGhostFrame(
                            targetGhost.state, targetGhost.jumping, targetGhost.falling, moving, (frameCounter / 5));

                    if (targetGhost.facing == 1) { // Prawa
                        gc.drawImage(ghostImg, currentPos[0], currentPos[1], ghostW, ghostH);
                    } else { // Lewa
                        gc.drawImage(ghostImg, currentPos[0] + ghostW, currentPos[1], -ghostW, ghostH);
                    }
                }

                gc.setGlobalAlpha(1.0);
            }
            // ===========================================

            gc.restore(); // Restore rendering context for static UI elements

            // Draw Score (Coins)
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 40));
            gc.fillText("Coins: " + coins, 20, 50);
        } else if (state == GameState.GAME_OVER) {
            gc.setFill(Color.BLACK);
            gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 60));
            gc.fillText("GAME OVER", canvas.getWidth() / 2 - 180, 200);

            gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 40));
            gc.fillText("Twój wynik:", canvas.getWidth() / 2 - 120, 350);
            gc.setFill(Color.YELLOW);
            gc.fillText("Monety: " + coins, canvas.getWidth() / 2 - 120, 420);
            gc.setFill(Color.RED);
            gc.fillText("Pokonane Goomby: " + goombasDefeated, canvas.getWidth() / 2 - 120, 490);
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
                    Game.lastFPS = frames;
                    
                    if (Game.menuIndex == 1 && Game.gameClient != null && Game.gameClient.connected) {
                        System.out.println("FPS: " + frames + " | Ticks: " + ticks + " | Ping: " + ping + " ms");
                    } else {
                        System.out.println("FPS: " + frames + " | Ticks: " + ticks);
                    }
                    
                    frames = 0;
                    ticks = 0;
                    
                    if (Game.menuIndex == 1 && Game.gameClient != null && Game.gameClient.connected) {
                        Game.lastPingSentTime = System.currentTimeMillis();
                        Game.gameClient.sendPacket(com.mario.net.Packet.ping(Game.lastPingSentTime));
                    }
                }
            }
        }.start();
    }

    @Override
    public void stop() {
        System.out.println("Zamykanie gry, zapisywanie bazy danych na serwerze i generowanie raportu...");
        try {
            // Zapis danych do bazy na Serwerze
            try {
                java.net.Socket s = new java.net.Socket(SERVER_IP, 1234);
                java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(s.getOutputStream());

                String payload = coins + ";" + goombasDefeated + ";3;Level1";
                out.writeObject(com.mario.net.Packet.saveDb(playerName, payload));
                out.flush();

                s.close();
                System.out.println("Zapisano pomyślnie na serwerze.");
            } catch (Exception ex) {
                System.out.println("Brak połączenia z serwerem. Zapis bazy niemożliwy.");
            }

            // Raport lokalny pozostaje bez zmian
            com.mario.io.GameFileManager gfm = new com.mario.io.GameFileManager();
            String report = "=== RAPORT Z DZIAŁANIA PROGRAMU ===\n" +
                    "Gracz: " + playerName + "\n" +
                    "Zebrane monety: " + coins + "\n" +
                    "Pokonane Goomby: " + goombasDefeated + "\n" +
                    "Status sieci: "
                    + ((gameClient != null && gameClient.connected) ? "Połączono"
                            : "Brak połączenia lub gra Single Player")
                    + "\n" +
                    "Kod pokoju: " + lobbyCode + "\n" +
                    "Czas zakonczenia: " + java.time.LocalDateTime.now().toString() + "\n" +
                    "===================================\n";
            gfm.writeReport("reports", report);
            System.out.println("Raport zapisany pomyślnie.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * The entry point of the application.
     */
    public static void main(String[] args) {
        if (args.length > 0) {
            SERVER_IP = args[0];
        }
        launch(args);
    }
}
