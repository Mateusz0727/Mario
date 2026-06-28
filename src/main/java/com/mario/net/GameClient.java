package com.mario.net;

import com.mario.Game;
import javafx.application.Platform;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class GameClient extends Thread {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    public boolean connected = false;

    // Local Map of network ghosts
    public ConcurrentHashMap<String, GameData> ghosts = new ConcurrentHashMap<>();

    public GameClient(String host, int port) {
        try {
            socket = new Socket(host, port);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            connected = true;
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Nie można połączyć się z serwerem!");
        }
    }

    public void disconnect() {
        connected = false;
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void sendPacket(Packet p) {
        if (!connected) return;
        try {
            out.reset();
            out.writeObject(p);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        if (!connected) return;
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof Packet) {
                    Packet packet = (Packet) obj;

                    if (packet.type == Packet.Type.JOIN_ROOM) {
                        Game.lobbyCode = packet.roomCode;
                    } 
                    else if (packet.type == Packet.Type.ERROR) {
                        System.err.println("Błąd z Serwera: " + packet.message);
                        Platform.runLater(() -> {
                            javafx.scene.control.Alert a = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
                            a.setContentText(packet.message);
                            a.show();
                            Game.state = Game.GameState.MENU;
                        });
                        break;
                    }
                    else if (packet.type == Packet.Type.START_GAME) {
                        System.out.println("Gracz 2 dołączył! Rozpoczynamy mapę.");
                        Platform.runLater(() -> {
                            Game.currentLevel = 1;
                            Game.resetLevel();
                            Game.state = Game.GameState.PLAYING;
                        });
                    }
                    else if (packet.type == Packet.Type.UPDATE) {
                        ghosts.put(packet.data.playerId, packet.data);
                    }
                    else if (packet.type == Packet.Type.TILE_SYNC) {
                        Platform.runLater(() -> {
                            Game.handler.updateTileState(packet.tx, packet.ty, packet.tileRemoved, packet.tileActivated);
                        });
                    }
                    else if (packet.type == Packet.Type.ENTITY_SYNC) {
                        Platform.runLater(() -> {
                            Game.handler.updateEntityState(packet.tx, packet.ty, packet.tileRemoved);
                        });
                    }
                    else if (packet.type == Packet.Type.SERVER_GOOMBA_SPAWN) {
                        Platform.runLater(() -> {
                            com.mario.entity.mob.Goomba g = new com.mario.entity.mob.Goomba(packet.gx, packet.gy, 64, 64, true, com.mario.Id.goomba, Game.handler);
                            g.netId = packet.entityId;
                            g.serverX = packet.gx;
                            g.serverY = packet.gy;
                            Game.handler.addEntity(g);
                        });
                    }
                    else if (packet.type == Packet.Type.SERVER_GOOMBA_UPDATE) {
                        Platform.runLater(() -> {
                            for (int i = 0; i < Game.handler.entity.size(); i++) {
                                com.mario.entity.Entity e = Game.handler.entity.get(i);
                                if (e.getId() == com.mario.Id.goomba && e.netId == packet.entityId) {
                                    com.mario.entity.mob.Goomba g = (com.mario.entity.mob.Goomba) e;
                                    g.serverX = packet.gx;
                                    g.serverY = packet.gy;
                                    if (packet.gDying && !g.dying) {
                                        g.die();
                                    }
                                }
                            }
                        });
                    }
                    else if (packet.type == Packet.Type.SERVER_GOOMBA_DIE) {
                        Platform.runLater(() -> {
                            for (int i = 0; i < Game.handler.entity.size(); i++) {
                                com.mario.entity.Entity e = Game.handler.entity.get(i);
                                if (e.getId() == com.mario.Id.goomba && e.netId == packet.entityId) {
                                    e.die();
                                }
                            }
                        });
                    }
                    else if (packet.type == Packet.Type.SERVER_MUSHROOM_SPAWN) {
                        Platform.runLater(() -> {
                            com.mario.entity.powerup.Mushroom m = new com.mario.entity.powerup.Mushroom(packet.gx, packet.gy, 64, 64, true, com.mario.Id.mushroom, Game.handler);
                            m.netId = packet.entityId;
                            m.serverX = packet.gx;
                            m.serverY = packet.gy;
                            Game.handler.addEntity(m);
                        });
                    }
                    else if (packet.type == Packet.Type.SERVER_MUSHROOM_UPDATE) {
                        Platform.runLater(() -> {
                            for (int i = 0; i < Game.handler.entity.size(); i++) {
                                com.mario.entity.Entity e = Game.handler.entity.get(i);
                                if (e.getId() == com.mario.Id.mushroom && e.netId == packet.entityId) {
                                    com.mario.entity.powerup.Mushroom m = (com.mario.entity.powerup.Mushroom) e;
                                    m.serverX = packet.gx;
                                    m.serverY = packet.gy;
                                }
                            }
                        });
                    }
                    else if (packet.type == Packet.Type.SERVER_MUSHROOM_DIE) {
                        Platform.runLater(() -> {
                            for (int i = 0; i < Game.handler.entity.size(); i++) {
                                com.mario.entity.Entity e = Game.handler.entity.get(i);
                                if (e.getId() == com.mario.Id.mushroom && e.netId == packet.entityId) {
                                    e.die();
                                }
                            }
                        });
                    }
                    else if (packet.type == Packet.Type.PONG) {
                        try {
                            long sentTime = Long.parseLong(packet.message);
                            Game.ping = (int) (System.currentTimeMillis() - sentTime);
                        } catch (Exception e) {}
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Rozłączono z serwerem trybu Multiplayer.");
            Platform.runLater(() -> Game.state = Game.GameState.MENU);
        }
    }
}
