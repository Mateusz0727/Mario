package com.marioserver;

import com.mario.net.Packet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class Room implements Runnable {
    public String roomCode;
    public List<ClientHandler> clients;
    public boolean isGameStarted;
    
    private Thread gameThread;
    private boolean running = false;
    
    // Server logic
    private List<ServerGoomba> goombas;
    private List<ServerMushroom> mushrooms;
    private int spawnerTickCount = 0;
    private int nextSpawnTime = 180;
    private Random randomSpawner = new Random(1337);
    private int entityIdCounter = 1000;

    public Room(String roomCode) {
        this.roomCode = roomCode;
        this.clients = new ArrayList<>();
        this.goombas = new CopyOnWriteArrayList<>();
        this.mushrooms = new CopyOnWriteArrayList<>();
        this.isGameStarted = false;
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        if (clients.isEmpty()) {
            stopGameLoop();
        }
    }

    public synchronized List<ClientHandler> getOtherClients(ClientHandler current) {
        List<ClientHandler> others = new ArrayList<>();
        for (ClientHandler c : clients) {
            if (c != current) {
                others.add(c);
            }
        }
        return others;
    }

    public synchronized void broadcast(Object obj) {
        for (ClientHandler client : clients) {
            client.sendObject(obj);
        }
    }
    
    public void startGameLoop() {
        if (!running) {
            running = true;
            gameThread = new Thread(this);
            gameThread.start();
        }
    }
    
    public void stopGameLoop() {
        running = false;
    }

    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double delta = 0;
        double ns = 1_000_000_000.0 / 60.0;
        
        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            
            while (delta >= 1) {
                tick();
                delta--;
            }
            
            try {
                Thread.sleep(2);
            } catch (Exception e) {}
        }
    }
    
    private void tick() {
        if (!isGameStarted) return;
        
        // Goomba Spawner
        spawnerTickCount++;
        if (spawnerTickCount >= nextSpawnTime) {
            spawnerTickCount = 0;
            nextSpawnTime = randomSpawner.nextInt(300) + 180;

            int pipeSide = randomSpawner.nextInt(2);
            double spawnX = (pipeSide == 0) ? (3 * 64) : (26 * 64 - 64);
            double spawnY = 2 * 64;
            
            int newId = entityIdCounter++;
            ServerGoomba g = new ServerGoomba(newId, spawnX, spawnY);
            goombas.add(g);
            
            broadcast(Packet.serverGoombaSpawn(roomCode, newId, spawnX, spawnY));
        }
        
        // Update Goombas
        for (ServerGoomba g : goombas) {
            g.tick();
            
            broadcast(Packet.serverGoombaUpdate(roomCode, g.id, g.x, g.y, g.dying));
            
            if (g.dead) {
                goombas.remove(g);
            }
        }
        
        // Update Mushrooms
        for (ServerMushroom m : mushrooms) {
            m.tick();
            
            broadcast(Packet.serverMushroomUpdate(roomCode, m.id, m.x, m.y, m.dying));
            
            if (m.dead) {
                mushrooms.remove(m);
            }
        }
    }
    
    public void killGoomba(int id) {
        for (ServerGoomba g : goombas) {
            if (g.id == id && !g.dying) {
                g.die();
                broadcast(Packet.serverGoombaDie(roomCode, id));
                break;
            }
        }
    }

    public void spawnMushroom(double x, double y) {
        int newId = entityIdCounter++;
        ServerMushroom m = new ServerMushroom(newId, x, y);
        mushrooms.add(m);
        broadcast(Packet.serverMushroomSpawn(roomCode, newId, x, y));
    }

    public void killMushroom(int id) {
        for (ServerMushroom m : mushrooms) {
            if (m.id == id && !m.dying) {
                m.die();
                broadcast(Packet.serverMushroomDie(roomCode, id));
                break;
            }
        }
    }
}
