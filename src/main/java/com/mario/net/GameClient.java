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
                }
            }
        } catch (Exception e) {
            System.out.println("Rozłączono z serwerem trybu Multiplayer.");
            Platform.runLater(() -> Game.state = Game.GameState.MENU);
        }
    }
}
